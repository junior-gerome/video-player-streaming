package com.videoplayer.service;

import com.videoplayer.dto.DubJobDTO;
import com.videoplayer.dto.DubSegmentDTO;
import com.videoplayer.model.DubJob;
import com.videoplayer.model.DubSegment;
import com.videoplayer.model.Video;
import com.videoplayer.repository.DubJobRepository;
import com.videoplayer.repository.DubSegmentRepository;
import com.videoplayer.websocket.DubWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DubService {

    private static final Logger log = LoggerFactory.getLogger(DubService.class);

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_STREAMING = "STREAMING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_ERROR = "ERROR";

    private final Map<String, List<DubSegmentDTO>> liveSegments = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();
    private final Map<String, BufferedWriter> activeProcessWriters = new ConcurrentHashMap<>();
    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "dub-worker");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private DubJobRepository dubJobRepository;

    @Autowired
    private DubSegmentRepository dubSegmentRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private VideoService videoService;

    @Autowired
    @Lazy
    private DubWebSocketHandler dubWebSocketHandler;

    @Value("${dub.python}")
    private String python;

    @Value("${dub.script}")
    private String script;

    @Value("${dub.model:base}")
    private String model;

    @Value("${dub.voice:fr-FR-HenriNeural}")
    private String voice;

    @Value("${dub.temp-dir:./dub-tmp}")
    private String tempDir;

    @jakarta.annotation.PostConstruct
    public void cleanupOrphanedJobs() {
        try {
            List<DubJob> all = dubJobRepository.findAll();
            for (DubJob job : all) {
                if (STATUS_PENDING.equals(job.getStatus()) || STATUS_PROCESSING.equals(job.getStatus()) || STATUS_STREAMING.equals(job.getStatus())) {
                    log.info("Réinitialisation du job orphelin id={} vidéo={}", job.getId(), job.getVideoId());
                    job.setStatus(STATUS_ERROR);
                    job.setMessage("Traitement interrompu");
                    job.setError("Le traitement a été interrompu par le redémarrage du serveur. Vous pouvez relancer le doublage.");
                    dubJobRepository.save(job);
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors du nettoyage des jobs orphelins", e);
        }
    }

    public DubJobDTO createDubJob(Long videoId, String targetLang) {
        return createDubJobWithOptions(videoId, targetLang, false, false, true);
    }

    public DubJobDTO createDubJob(Long videoId, String targetLang, boolean force) {
        return createDubJobWithOptions(videoId, targetLang, force, false, true);
    }

    public DubJobDTO createDubJobWithOptions(Long videoId, String targetLang, boolean force, boolean cloneVoice, boolean demucs) {
        String lang = (targetLang == null || targetLang.isBlank()) ? "fr" : targetLang.trim().toLowerCase();
        String jobKey = videoId + "_" + lang;
        Optional<DubJob> existing = dubJobRepository.findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(videoId, lang);

        Process runningProc = activeProcesses.get(jobKey);
        boolean procAlive = runningProc != null && runningProc.isAlive();

        if (existing.isPresent() && !force) {
            DubJob ej = existing.get();
            String status = ej.getStatus();
            if (STATUS_READY.equals(status)) {
                preloadSegmentsFromDb(videoId, lang);
                return toDTO(ej);
            }
            if ((STATUS_PENDING.equals(status) || STATUS_PROCESSING.equals(status) || STATUS_STREAMING.equals(status)) && procAlive) {
                preloadSegmentsFromDb(videoId, lang);
                return toDTO(ej);
            }
        }

        if (procAlive) {
            log.info("Arrêt de l'ancien processus actif pour {}", jobKey);
            try {
                runningProc.destroyForcibly();
            } catch (Exception ignored) {}
            activeProcesses.remove(jobKey);
            activeProcessWriters.remove(jobKey);
        }

        List<DubJob> oldJobs = dubJobRepository.findAllByVideoIdAndTargetLangOrderByCreatedAtDesc(videoId, lang);
        if (!oldJobs.isEmpty()) {
            try {
                dubJobRepository.deleteAll(oldJobs);
            } catch (Exception ignored) {}
        }

        if (force) {
            dubSegmentRepository.deleteByVideoIdAndTargetLang(videoId, lang);
            liveSegments.remove(jobKey);
        } else {
            preloadSegmentsFromDb(videoId, lang);
        }

        Video video = videoService.getVideoEntity(videoId);
        DubJob job = new DubJob();
        job.setVideoId(videoId);
        job.setVideoTitle(video.getTitle());
        job.setTargetLang(lang);
        job.setCloneVoice(cloneVoice);
        job.setDemucs(demucs);
        job.setStatus(STATUS_PENDING);
        job.setProgress(0);
        job.setMessage("En file d'attente (" + lang.toUpperCase() + ")");
        job = dubJobRepository.save(job);

        DubJob finalJob = job;
        executor.submit(() -> processJob(finalJob.getId()));
        return toDTO(finalJob);
    }

    private void preloadSegmentsFromDb(Long videoId, String lang) {
        String key = videoId + "_" + lang.trim().toLowerCase();
        List<DubSegment> dbSegs = dubSegmentRepository.findByVideoIdAndTargetLangOrderBySegmentIndexAsc(videoId, lang);
        if (!dbSegs.isEmpty()) {
            List<DubSegmentDTO> dtoList = new CopyOnWriteArrayList<>();
            for (DubSegment seg : dbSegs) {
                dtoList.add(new DubSegmentDTO(seg.getSegmentIndex(), seg.getStartTime(), seg.getEndTime(), seg.getAudioUrl()));
            }
            liveSegments.put(key, dtoList);
        }
    }

    public List<DubSegmentDTO> getSegments(Long videoId, String lang) {
        if (videoId == null || lang == null) return Collections.emptyList();
        String key = videoId + "_" + lang.trim().toLowerCase();
        List<DubSegmentDTO> list = liveSegments.get(key);
        if (list != null && !list.isEmpty()) {
            return new ArrayList<>(list);
        }

        List<DubSegment> dbList = dubSegmentRepository.findByVideoIdAndTargetLangOrderBySegmentIndexAsc(videoId, lang.trim().toLowerCase());
        if (!dbList.isEmpty()) {
            List<DubSegmentDTO> converted = new CopyOnWriteArrayList<>();
            for (DubSegment s : dbList) {
                converted.add(new DubSegmentDTO(s.getSegmentIndex(), s.getStartTime(), s.getEndTime(), s.getAudioUrl()));
            }
            liveSegments.put(key, converted);
            return new ArrayList<>(converted);
        }
        return Collections.emptyList();
    }

    public String getSegmentObjectName(Long videoId, String lang, int index) {
        return "dub/" + videoId + "/" + lang.trim().toLowerCase() + "/seg_" + index + ".mp3";
    }

    public DubJobDTO getJobForVideo(Long videoId) {
        Optional<DubJob> job = dubJobRepository.findFirstByVideoIdOrderByCreatedAtDesc(videoId);
        return job.map(this::toDTO).orElse(null);
    }

    public DubJobDTO getJobForVideo(Long videoId, String targetLang) {
        if (targetLang == null || targetLang.isBlank()) {
            return getJobForVideo(videoId);
        }
        Optional<DubJob> job = dubJobRepository.findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(videoId, targetLang.trim().toLowerCase());
        return job.map(this::toDTO).orElse(null);
    }

    public List<DubJobDTO> getAllJobsForVideo(Long videoId) {
        List<DubJob> jobs = dubJobRepository.findAllByVideoIdOrderByCreatedAtDesc(videoId);
        List<DubJobDTO> result = new ArrayList<>();
        for (DubJob job : jobs) {
            result.add(toDTO(job));
        }
        return result;
    }

    public DubJobDTO getJob(Long jobId) {
        return dubJobRepository.findById(jobId).map(this::toDTO).orElse(null);
    }

    public String getReadyAudioObjectName(Long videoId, String targetLang) {
        String lang = (targetLang == null || targetLang.isBlank()) ? null : targetLang.trim().toLowerCase();
        if (lang != null) {
            Optional<DubJob> job = dubJobRepository.findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(videoId, lang);
            if (job.isPresent() && (STATUS_READY.equals(job.get().getStatus()) || STATUS_STREAMING.equals(job.get().getStatus()))) {
                return job.get().getAudioObjectName();
            }
        }
        Optional<DubJob> fallback = dubJobRepository.findFirstByVideoIdOrderByCreatedAtDesc(videoId);
        if (fallback.isPresent() && (STATUS_READY.equals(fallback.get().getStatus()) || STATUS_STREAMING.equals(fallback.get().getStatus()))) {
            return fallback.get().getAudioObjectName();
        }
        return null;
    }

    public String getReadyAudioObjectName(Long videoId) {
        return getReadyAudioObjectName(videoId, null);
    }

    public void deleteDubsForVideo(Long videoId) {
        liveSegments.keySet().removeIf(k -> k.startsWith(videoId + "_"));
        dubSegmentRepository.deleteByVideoId(videoId);
        for (String key : activeProcesses.keySet()) {
            if (key.startsWith(videoId + "_")) {
                Process p = activeProcesses.remove(key);
                if (p != null) {
                    try { p.destroyForcibly(); } catch (Exception ignored) {}
                }
                activeProcessWriters.remove(key);
            }
        }
        List<DubJob> jobs = dubJobRepository.findAllByVideoIdOrderByCreatedAtDesc(videoId);
        for (DubJob job : jobs) {
            if (job.getAudioObjectName() != null) {
                try {
                    minioService.deleteFile(job.getAudioObjectName());
                } catch (Exception e) {
                    log.warn("Impossible de supprimer l'objet doublage {}", job.getAudioObjectName(), e);
                }
            }
        }
        dubJobRepository.deleteAll(jobs);
    }

    public String resolveVoice(String lang) {
        if (lang == null) return "fr-FR-HenriNeural";
        return switch (lang.toLowerCase()) {
            case "de" -> "de-DE-ConradNeural";
            case "es" -> "es-ES-AlvaroNeural";
            case "it" -> "it-IT-DiegoNeural";
            case "en" -> "en-US-GuyNeural";
            default -> (voice != null && !voice.isBlank()) ? voice : "fr-FR-HenriNeural";
        };
    }

    public void prioritizeSeek(Long videoId, String targetLang, double position) {
        String lang = (targetLang == null || targetLang.isBlank()) ? "fr" : targetLang.trim().toLowerCase();
        String key = videoId + "_" + lang;
        BufferedWriter writer = activeProcessWriters.get(key);
        if (writer != null) {
            try {
                writer.write("SEEK " + position + "\n");
                writer.flush();
                log.info("Priorité de seek transmise au pipeline: {}s (vidéo {}, lang {})", position, videoId, lang);
            } catch (Exception e) {
                log.warn("Impossible d'envoyer la commande de seek au pipeline: {}", e.getMessage());
            }
        }
    }

    public SseEmitter registerSseEmitter(Long videoId, String targetLang) {
        String lang = (targetLang == null || targetLang.isBlank()) ? "fr" : targetLang.trim().toLowerCase();
        String key = videoId + "_" + lang;
        SseEmitter emitter = new SseEmitter(180_000L);

        sseEmitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> list = sseEmitters.get(key);
            if (list != null) {
                list.remove(emitter);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            DubJobDTO job = getJobForVideo(videoId, lang);
            if (job != null) {
                emitter.send(SseEmitter.event().name("job").data(job));
            }
            List<DubSegmentDTO> existing = getSegments(videoId, lang);
            if (!existing.isEmpty()) {
                emitter.send(SseEmitter.event().name("segments").data(existing));
            }
        } catch (Exception ignored) {}

        return emitter;
    }

    private void broadcastSegment(Long videoId, String targetLang, DubSegmentDTO segment) {
        String key = videoId + "_" + targetLang.trim().toLowerCase();
        // SSE
        List<SseEmitter> list = sseEmitters.get(key);
        if (list != null && !list.isEmpty()) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("segment").data(segment));
                } catch (Exception e) {
                    list.remove(emitter);
                }
            }
        }
        // WebSocket bidirectionnel
        if (dubWebSocketHandler != null) {
            dubWebSocketHandler.broadcastSegment(videoId, targetLang, segment);
        }
    }

    private void broadcastJob(DubJobDTO job) {
        String key = job.getVideoId() + "_" + job.getTargetLang().trim().toLowerCase();
        // SSE
        List<SseEmitter> list = sseEmitters.get(key);
        if (list != null && !list.isEmpty()) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("job").data(job));
                } catch (Exception e) {
                    list.remove(emitter);
                }
            }
        }
        // WebSocket bidirectionnel
        if (dubWebSocketHandler != null) {
            dubWebSocketHandler.broadcastJob(job);
        }
    }

    private void processJob(Long jobId) {
        DubJob job = dubJobRepository.findById(jobId).orElse(null);
        if (job == null) return;
        Path work = null;
        String jobKey = job.getVideoId() + "_" + job.getTargetLang().toLowerCase();
        try {
            job.setStatus(STATUS_PROCESSING);
            job.setProgress(0);
            job.setMessage("Préparation");
            job.setError(null);
            dubJobRepository.save(job);
            broadcastJob(toDTO(job));

            Path base = Paths.get(tempDir);
            Files.createDirectories(base);
            work = Files.createTempDirectory(base, "dub_" + jobId + "_");
            Path inVideo = work.resolve("input.mp4");
            Path outAudio = work.resolve("dub.mp3");

            Path audioCacheDir = base.resolve("audio_cache");
            Files.createDirectories(audioCacheDir);
            Path cachedWav = audioCacheDir.resolve("video_" + job.getVideoId() + ".wav");

            Path transcriptCacheDir = base.resolve("transcript_cache");
            Files.createDirectories(transcriptCacheDir);
            Path cachedTranscript = transcriptCacheDir.resolve("video_" + job.getVideoId() + "_transcript.json");

            if (!Files.exists(cachedWav)) {
                Video video = videoService.getVideoEntity(job.getVideoId());
                try (InputStream is = minioService.getFile(video.getObjectName())) {
                    Files.copy(is, inVideo, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            List<String> cmd = new ArrayList<>();
            cmd.add(python);
            cmd.add("-u");
            cmd.add(script);
            cmd.add("--input");
            cmd.add(Files.exists(cachedWav) ? cachedWav.toString() : inVideo.toString());
            cmd.add("--audio");
            cmd.add(cachedWav.toString());
            cmd.add("--output");
            cmd.add(outAudio.toString());
            cmd.add("--source-lang");
            cmd.add("auto");
            cmd.add("--target-lang");
            cmd.add(job.getTargetLang());
            cmd.add("--model");
            cmd.add(model);
            cmd.add("--voice");
            cmd.add(resolveVoice(job.getTargetLang()));
            cmd.add("--workdir");
            cmd.add(work.toString());
            cmd.add("--model-cache");
            cmd.add(Paths.get(tempDir, "whisper").toString());
            cmd.add("--transcript-cache");
            cmd.add(cachedTranscript.toString());

            if (job.getCloneVoice()) {
                cmd.add("--clone-voice");
            }
            if (!job.getDemucs()) {
                cmd.add("--no-demucs");
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");
            pb.environment().put("PYTHONUNBUFFERED", "1");
            Process proc = pb.start();
            activeProcesses.put(jobKey, proc);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));
            activeProcessWriters.put(jobKey, writer);

            StringBuilder console = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("SEGMENT_CHUNK ")) {
                        try {
                            String[] parts = line.split(" ", 5);
                            if (parts.length >= 5) {
                                int index = Integer.parseInt(parts[1]);
                                double start = Double.parseDouble(parts[2]);
                                double end = Double.parseDouble(parts[3]);
                                Path segPath = Paths.get(parts[4]);
                                if (Files.exists(segPath)) {
                                    String segObjectName = "dub/" + job.getVideoId() + "/" + job.getTargetLang() + "/seg_" + index + ".mp3";
                                    try (InputStream is = Files.newInputStream(segPath)) {
                                        minioService.uploadFile(segObjectName, is, Files.size(segPath), "audio/mpeg");
                                    }
                                    String audioUrl = "/api/dub/" + job.getVideoId() + "/segments/" + index + "?lang=" + job.getTargetLang();
                                    DubSegmentDTO segDto = new DubSegmentDTO(index, start, end, audioUrl);

                                    DubSegment dbSeg = dubSegmentRepository
                                            .findByVideoIdAndTargetLangAndSegmentIndex(job.getVideoId(), job.getTargetLang(), index)
                                            .orElse(new DubSegment());
                                    dbSeg.setVideoId(job.getVideoId());
                                    dbSeg.setTargetLang(job.getTargetLang());
                                    dbSeg.setVoice(job.getCloneVoice() ? "cloned" : resolveVoice(job.getTargetLang()));
                                    dbSeg.setSegmentIndex(index);
                                    dbSeg.setStartTime(start);
                                    dbSeg.setEndTime(end);
                                    dbSeg.setAudioObjectName(segObjectName);
                                    dbSeg.setAudioUrl(audioUrl);
                                    dubSegmentRepository.save(dbSeg);

                                    liveSegments.computeIfAbsent(jobKey, k -> new CopyOnWriteArrayList<>()).add(segDto);
                                    job.setStatus(STATUS_STREAMING);
                                    dubJobRepository.save(job);

                                    // Diffusion instantanée vers WebSocket et SSE
                                    broadcastSegment(job.getVideoId(), job.getTargetLang(), segDto);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Erreur lors de l'upload du chunk temps réel", e);
                        }
                    } else if (line.startsWith("PROGRESS_STREAM ") || line.startsWith("PROGRESS ")) {
                        boolean isStream = line.startsWith("PROGRESS_STREAM ");
                        String[] parts = line.split(" ", 3);
                        try {
                            job.setProgress(Integer.parseInt(parts[1]));
                        } catch (Exception ignored) {}
                        if (parts.length > 2) {
                            job.setMessage(sanitize(parts[2].trim()));
                        }
                        if (isStream) {
                            job.setStatus(STATUS_STREAMING);
                        }
                        dubJobRepository.save(job);
                        broadcastJob(toDTO(job));
                    }
                    console.append(line).append('\n');
                }
            }
            activeProcessWriters.remove(jobKey);

            int exit = proc.waitFor();
            if (exit != 0) {
                String tail = console.length() > 800 ? console.substring(0, 800) : console.toString();
                throw new RuntimeException("Échec du pipeline (code " + exit + "): " + tail);
            }
            if (!Files.exists(outAudio)) {
                throw new RuntimeException("Le pipeline n'a produit aucune piste audio");
            }

            String audioObjectName = "dub/" + job.getVideoId() + "_" + job.getTargetLang() + ".mp3";
            try (InputStream is = Files.newInputStream(outAudio)) {
                minioService.uploadFile(audioObjectName, is, Files.size(outAudio), "audio/mpeg");
            }
            job.setAudioObjectName(audioObjectName);
            job.setStatus(STATUS_READY);
            job.setProgress(100);
            job.setMessage("Doublage prêt");
            job.setError(null);
            dubJobRepository.save(job);

            DubJobDTO readyDto = toDTO(job);
            broadcastJob(readyDto);
            if (dubWebSocketHandler != null) {
                dubWebSocketHandler.broadcastAudioReady(job.getVideoId(), job.getTargetLang(), readyDto.getAudioUrl());
            }
        } catch (Exception e) {
            log.error("Échec du doublage pour la vidéo {}", job.getVideoId(), e);
            job.setStatus(STATUS_ERROR);
            job.setError(sanitize(e.getMessage()));
            job.setMessage("Échec du doublage");
            try {
                dubJobRepository.save(job);
                broadcastJob(toDTO(job));
            } catch (Exception saveErr) {
                log.error("Impossible de persister l'état d'erreur du doublage", saveErr);
            }
        } finally {
            activeProcessWriters.remove(jobKey);
            activeProcesses.remove(jobKey);
            if (work != null) {
                deleteRecursively(work);
            }
        }
    }

    private static String sanitize(String s) {
        if (s == null) return null;
        return s.replace("\uFFFD", "?")
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .trim();
    }

    private void deleteRecursively(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (var paths = Files.walk(dir)) {
                    paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Nettoyage du dossier temporaire impossible", e);
        }
    }

    public DubJobDTO toDTO(DubJob job) {
        DubJobDTO dto = new DubJobDTO();
        dto.setId(job.getId());
        dto.setVideoId(job.getVideoId());
        dto.setVideoTitle(job.getVideoTitle());
        dto.setTargetLang(job.getTargetLang());
        dto.setStatus(job.getStatus());
        dto.setProgress(job.getProgress());
        dto.setMessage(job.getMessage());
        dto.setError(job.getError());
        dto.setCloneVoice(job.getCloneVoice());
        dto.setDemucs(job.getDemucs());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        if (STATUS_READY.equals(job.getStatus())) {
            dto.setAudioUrl("/api/dub/" + job.getVideoId() + "/audio?lang=" + job.getTargetLang());
        }
        return dto;
    }
}