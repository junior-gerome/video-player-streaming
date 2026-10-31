package com.videoplayer.controller;

import com.videoplayer.dto.VideoDTO;
import com.videoplayer.service.DubService;
import com.videoplayer.service.MinioService;
import com.videoplayer.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DubService dubService;

    @PostMapping("/upload")
    public ResponseEntity<VideoDTO> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {
        VideoDTO video = videoService.uploadVideo(file, title, description);
        try {
            dubService.createDubJob(video.getId(), "fr");
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(video);
    }

    @GetMapping
    public ResponseEntity<Page<VideoDTO>> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(videoService.getAllVideos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range) {

        VideoDTO videoDTO = videoService.getVideoById(id);

        long fileSize = videoDTO.getFileSize();
        String contentType = videoDTO.getContentType() != null ? videoDTO.getContentType() : "video/mp4";
        MediaType mediaType = MediaType.parseMediaType(contentType);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept-Ranges", "bytes");

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
            Resource resource = videoService.streamVideoRange(id, start, contentLength);

            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(contentLength);
            headers.setContentType(mediaType);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(resource);
        }

        Resource resource = videoService.streamVideo(id);
        headers.setContentLength(fileSize);
        headers.setContentType(mediaType);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VideoDTO>> searchVideos(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(videoService.searchVideos(q, pageable));
    }
}
