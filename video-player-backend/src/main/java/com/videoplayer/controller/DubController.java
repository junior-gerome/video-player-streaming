package com.videoplayer.controller;

import com.videoplayer.dto.ArgosModelDTO;
import com.videoplayer.service.ArgosModelService;
import com.videoplayer.dto.DubJobDTO;
import com.videoplayer.dto.DubSegmentDTO;
import com.videoplayer.service.DubService;
import com.videoplayer.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dub")
public class DubController {

    @Autowired
    private DubService dubService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private ArgosModelService argosModelService;

    @GetMapping("/models/argos")
    public ResponseEntity<List<ArgosModelDTO>> getArgosModels() {
        return ResponseEntity.ok(argosModelService.getAvailableModels());
    }

    @PostMapping("/models/argos/download/{lang}")
    public ResponseEntity<Map<String, Object>> downloadArgosModel(@PathVariable String lang) {
        argosModelService.downloadModelAsync(lang);
        return ResponseEntity.ok(Map.of("status", "DOWNLOADING", "lang", lang));
    }

    @PostMapping("/models/argos/download-all")
    public ResponseEntity<Map<String, Object>> downloadAllArgosModels() {
        argosModelService.downloadAllAsync();
        return ResponseEntity.ok(Map.of("status", "DOWNLOADING_ALL"));
    }

    @PostMapping("/{videoId}/generate")
    public ResponseEntity<DubJobDTO> generate(
            @PathVariable Long videoId,
            @RequestBody(required = false) Map<String, Object> body) {
        String targetLang = body != null && body.get("targetLang") != null ? body.get("targetLang").toString() : null;
        boolean force = body != null && (Boolean.TRUE.equals(body.get("force")) || "true".equalsIgnoreCase(String.valueOf(body.get("force"))));
        boolean cloneVoice = body != null && (Boolean.TRUE.equals(body.get("cloneVoice")) || "true".equalsIgnoreCase(String.valueOf(body.get("cloneVoice"))));
        boolean demucs = body == null || body.get("demucs") == null || Boolean.TRUE.equals(body.get("demucs")) || "true".equalsIgnoreCase(String.valueOf(body.get("demucs")));
        return ResponseEntity.ok(dubService.createDubJobWithOptions(videoId, targetLang, force, cloneVoice, demucs));
    }

    @PostMapping("/{videoId}/retry")
    public ResponseEntity<DubJobDTO> retry(
            @PathVariable Long videoId,
            @RequestBody(required = false) Map<String, Object> body) {
        String targetLang = body != null && body.get("targetLang") != null ? body.get("targetLang").toString() : null;
        boolean cloneVoice = body != null && (Boolean.TRUE.equals(body.get("cloneVoice")) || "true".equalsIgnoreCase(String.valueOf(body.get("cloneVoice"))));
        boolean demucs = body == null || body.get("demucs") == null || Boolean.TRUE.equals(body.get("demucs")) || "true".equalsIgnoreCase(String.valueOf(body.get("demucs")));
        return ResponseEntity.ok(dubService.createDubJobWithOptions(videoId, targetLang, true, cloneVoice, demucs));
    }

    @PostMapping("/{videoId}/seek")
    public ResponseEntity<Void> prioritizeSeek(
            @PathVariable Long videoId,
            @RequestBody(required = false) Map<String, Object> body) {
        String targetLang = body != null && body.get("targetLang") != null ? body.get("targetLang").toString() : "fr";
        double position = 0.0;
        if (body != null && body.get("position") != null) {
            try {
                position = Double.parseDouble(body.get("position").toString());
            } catch (Exception ignored) {}
        }
        dubService.prioritizeSeek(videoId, targetLang, position);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{videoId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeEvents(
            @PathVariable Long videoId,
            @RequestParam(required = false, defaultValue = "fr") String lang) {
        return dubService.registerSseEmitter(videoId, lang);
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<DubJobDTO> getForVideo(
            @PathVariable Long videoId,
            @RequestParam(required = false) String lang) {
        DubJobDTO dto = dubService.getJobForVideo(videoId, lang);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/video/{videoId}/tracks")
    public ResponseEntity<List<DubJobDTO>> getAllTracksForVideo(@PathVariable Long videoId) {
        List<DubJobDTO> tracks = dubService.getAllJobsForVideo(videoId);
        return ResponseEntity.ok(tracks);
    }

    @GetMapping("/{videoId}/segments")
    public ResponseEntity<List<DubSegmentDTO>> getSegments(
            @PathVariable Long videoId,
            @RequestParam(required = false, defaultValue = "fr") String lang) {
        return ResponseEntity.ok(dubService.getSegments(videoId, lang));
    }

    @GetMapping("/{videoId}/segments/{index}")
    public ResponseEntity<Resource> streamSegmentAudio(
            @PathVariable Long videoId,
            @PathVariable Integer index,
            @RequestParam(required = false, defaultValue = "fr") String lang) {
        String objectName = dubService.getSegmentObjectName(videoId, lang, index);
        try {
            long fileSize = minioService.getFileSize(objectName);
            InputStream inputStream = minioService.getFile(objectName);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable");
            headers.setETag("\"" + videoId + "-" + lang + "-" + index + "\"");
            headers.setContentLength(fileSize);
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<DubJobDTO> getById(@PathVariable Long jobId) {
        DubJobDTO dto = dubService.getJob(jobId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = {"/{videoId}/audio", "/{videoId}/audio/{lang}"})
    public ResponseEntity<Resource> streamAudio(
            @PathVariable Long videoId,
            @PathVariable(required = false) String lang,
            @RequestParam(name = "lang", required = false) String queryLang,
            @RequestHeader(value = "Range", required = false) String range) {
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang : queryLang;
        String objectName = dubService.getReadyAudioObjectName(videoId, effectiveLang);
        if (objectName == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            long fileSize = minioService.getFileSize(objectName);
            MediaType mediaType = MediaType.parseMediaType("audio/mpeg");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept-Ranges", "bytes");
            headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            if (range != null) {
                String[] ranges = range.replace("bytes=", "").split("-");
                long start;
                try {
                    start = Long.parseLong(ranges[0]);
                } catch (NumberFormatException e) {
                    start = 0;
                }
                long end = ranges.length > 1 && !ranges[1].isEmpty()
                        ? Long.parseLong(ranges[1])
                        : fileSize - 1;

                if (start >= fileSize || start > end) {
                    headers.add("Content-Range", "bytes */" + fileSize);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                if (end >= fileSize) {
                    end = fileSize - 1;
                }
                long contentLength = end - start + 1;
                InputStream is = minioService.getFileRange(objectName, start, contentLength);

                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                headers.setContentLength(contentLength);
                headers.setContentType(mediaType);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new InputStreamResource(is));
            }

            InputStream inputStream = minioService.getFile(objectName);
            headers.setContentLength(fileSize);
            headers.setContentType(mediaType);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/video/{videoId}")
    public ResponseEntity<Void> deleteForVideo(@PathVariable Long videoId) {
        dubService.deleteDubsForVideo(videoId);
        return ResponseEntity.noContent().build();
    }
}