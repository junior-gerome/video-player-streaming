package com.videoplayer.service;

import com.videoplayer.dto.VideoDTO;
import com.videoplayer.model.Video;
import com.videoplayer.repository.DubJobRepository;
import com.videoplayer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DubJobRepository dubJobRepository;

    public VideoDTO uploadVideo(MultipartFile file, String title, String description) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID() + extension;

        minioService.uploadFile(file, objectName);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setFilename(originalFilename);
        video.setObjectName(objectName);
        video.setContentType(file.getContentType());
        video.setFileSize(file.getSize());

        video = videoRepository.save(video);
        return toDTO(video);
    }

    public Page<VideoDTO> getAllVideos(Pageable pageable) {
        return videoRepository.findAll(pageable).map(this::toDTO);
    }

    public VideoDTO getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + id));
        return toDTO(video);
    }

    public Resource streamVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + id));
        InputStream inputStream = minioService.getFile(video.getObjectName());
        return new InputStreamResource(inputStream);
    }

    public Video getVideoEntity(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + id));
    }

    public Resource streamVideoRange(Long id, long start, long length) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + id));
        InputStream inputStream = minioService.getFileRange(video.getObjectName(), start, length);
        return new InputStreamResource(inputStream);
    }

    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + id));
        minioService.deleteFile(video.getObjectName());

        dubJobRepository.findAllByVideoIdOrderByCreatedAtDesc(id).forEach(job -> {
            if (job.getAudioObjectName() != null) {
                try {
                    minioService.deleteFile(job.getAudioObjectName());
                } catch (Exception e) {
                    // ignorer : l'objet audio peut ne pas exister
                }
            }
        });
        dubJobRepository.deleteAll(dubJobRepository.findAllByVideoIdOrderByCreatedAtDesc(id));

        videoRepository.delete(video);
    }

    public Page<VideoDTO> searchVideos(String query, Pageable pageable) {
        return videoRepository.findByTitleContainingIgnoreCase(query, pageable).map(this::toDTO);
    }

    private VideoDTO toDTO(Video video) {
        VideoDTO dto = new VideoDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setFilename(video.getFilename());
        dto.setContentType(video.getContentType());
        dto.setFileSize(video.getFileSize());
        dto.setDuration(video.getDuration());
        dto.setCreatedAt(video.getCreatedAt());
        dto.setStreamUrl("/api/videos/" + video.getId() + "/stream");
        return dto;
    }
}
