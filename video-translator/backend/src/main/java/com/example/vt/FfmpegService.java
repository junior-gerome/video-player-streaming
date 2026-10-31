package com.example.vt;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FfmpegService {

    // métadonnée "language" des pistes : code ISO 639-2 attendu par les lecteurs vidéo
    private static final Map<String, String> ISO3 = Map.of(
            "en", "eng", "fr", "fra", "es", "spa", "de", "deu", "ar", "ara", "pt", "por");

    public void extractAudio(Path video, Path audioOut) throws IOException, InterruptedException {
        run(List.of("ffmpeg", "-y", "-i", video.toString(), "-vn", "-ac", "1", "-ar", "16000",
                audioOut.toString()));
    }

    public long durationMs(Path media) throws IOException, InterruptedException {
        Process p = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "csv=p=0", media.toString()).start();
        String out = new String(p.getInputStream().readAllBytes()).trim();
        if (p.waitFor() != 0) throw new IOException("ffprobe a échoué");
        return Math.round(Double.parseDouble(out) * 1000);
    }

    /** Vidéo d'origine + audio d'origine + une piste doublée par langue. */
    public void mux(Path video, Map<String, Path> dubbedByLang, Path out)
            throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>(List.of("ffmpeg", "-y", "-i", video.toString()));
        dubbedByLang.values().forEach(p -> { cmd.add("-i"); cmd.add(p.toString()); });
        cmd.addAll(List.of("-map", "0:v", "-map", "0:a?"));
        for (int i = 1; i <= dubbedByLang.size(); i++) { cmd.add("-map"); cmd.add(i + ":a"); }
        cmd.addAll(List.of("-c:v", "copy", "-c:a", "aac"));
        int track = 1; // piste 0 = audio original
        for (String lang : dubbedByLang.keySet()) {
            cmd.add("-metadata:s:a:" + track++);
            cmd.add("language=" + ISO3.getOrDefault(lang, lang));
        }
        cmd.add(out.toString());
        run(cmd);
    }

    private void run(List<String> cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start(); // logs ffmpeg visibles
        if (p.waitFor() != 0) throw new IOException("ffmpeg a échoué : " + String.join(" ", cmd));
    }
}
