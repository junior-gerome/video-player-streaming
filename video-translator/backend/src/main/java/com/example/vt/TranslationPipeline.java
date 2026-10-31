package com.example.vt;

import com.example.vt.TranslationJob.Status;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TranslationPipeline {
    private final FfmpegService ffmpeg;
    private final AiServiceClient ai;

    public TranslationPipeline(FfmpegService ffmpeg, AiServiceClient ai) {
        this.ffmpeg = ffmpeg;
        this.ai = ai;
    }

    @Async
    public void run(TranslationJob job, Path video, Path dir) {
        try {
            job.setStatus(Status.EXTRACTING);
            Path audio = dir.resolve("source.mp3");
            ffmpeg.extractAudio(video, audio);
            long totalMs = ffmpeg.durationMs(video);

            job.setStatus(Status.TRANSCRIBING);          // une seule fois, quel que soit le nombre de langues
            var transcript = ai.transcribe(audio);

            job.setStatus(Status.DUBBING);
            Map<String, Path> dubbed = new LinkedHashMap<>();
            for (String lang : job.getLanguages()) {
                Path out = dir.resolve("dub_" + lang + ".wav");
                ai.dub(transcript, lang, totalMs, out);
                dubbed.put(lang, out);
            }

            job.setStatus(Status.MERGING);
            Path result = dir.resolve("result.mp4");
            ffmpeg.mux(video, dubbed, result);
            job.setResult(result);
            job.setStatus(Status.DONE);
        } catch (Exception e) {
            job.setError(e.getMessage());
            job.setStatus(Status.FAILED);
        }
    }
}
