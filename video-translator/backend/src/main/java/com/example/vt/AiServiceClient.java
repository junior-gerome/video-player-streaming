package com.example.vt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** Client du microservice Python (faster-whisper + Argos Translate + Piper). */
@Component
public class AiServiceClient {

    public record Segment(double start, double end, String text) {}
    public record Transcript(String language, List<Segment> segments) {}
    private record DubRequest(List<Segment> segments,
                              @JsonProperty("source_lang") String sourceLang,
                              @JsonProperty("target_lang") String targetLang,
                              @JsonProperty("total_ms") long totalMs) {}

    private final RestClient http;

    public AiServiceClient(@Value("${app.ai-service.url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofMinutes(30)); // transcription / doublage longs
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public Transcript transcribe(Path audio) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audio));
        return http.post().uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body).retrieve().body(Transcript.class);
    }

    /** Traduit + synthétise chaque segment, recale sur la timeline, écrit un WAV. */
    public void dub(Transcript t, String targetLang, long totalMs, Path out) throws IOException {
        byte[] wav = http.post().uri("/dub")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DubRequest(t.segments(), t.language(), targetLang, totalMs))
                .retrieve().body(byte[].class);
        Files.write(out, wav);
    }
}
