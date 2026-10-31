package com.example.vt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/translations")
@CrossOrigin(origins = "http://localhost:4200")
public class TranslationController {
    private final Map<String, TranslationJob> jobs = new ConcurrentHashMap<>();
    private final TranslationPipeline pipeline;
    private final Path workDir;

    public TranslationController(TranslationPipeline pipeline, @Value("${app.work-dir}") String workDir) {
        this.pipeline = pipeline;
        this.workDir = Path.of(workDir);
    }

    public record JobView(String id, String status, String error) {}

    @PostMapping
    public JobView create(@RequestParam MultipartFile file, @RequestParam List<String> languages)
            throws IOException {
        String id = UUID.randomUUID().toString();
        Path dir = Files.createDirectories(workDir.resolve(id));
        Path video = dir.resolve("input.mp4");
        file.transferTo(video);
        var job = new TranslationJob(id, languages);
        jobs.put(id, job);
        pipeline.run(job, video, dir);
        return view(job);
    }

    @GetMapping("/{id}")
    public JobView status(@PathVariable String id) { return view(find(id)); }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        var job = find(id);
        if (job.getResult() == null) throw new ResponseStatusException(NOT_FOUND, "Pas encore prêt");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"video-multilingue.mp4\"")
                .body(new FileSystemResource(job.getResult()));
    }

    private TranslationJob find(String id) {
        var job = jobs.get(id);
        if (job == null) throw new ResponseStatusException(NOT_FOUND);
        return job;
    }
    private JobView view(TranslationJob j) { return new JobView(j.getId(), j.getStatus().name(), j.getError()); }
}
