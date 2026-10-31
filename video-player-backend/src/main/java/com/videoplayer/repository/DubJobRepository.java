package com.videoplayer.repository;

import com.videoplayer.model.DubJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DubJobRepository extends JpaRepository<DubJob, Long> {
    Optional<DubJob> findFirstByVideoIdOrderByCreatedAtDesc(Long videoId);
    Optional<DubJob> findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(Long videoId, String targetLang);
    List<DubJob> findAllByVideoIdAndTargetLangOrderByCreatedAtDesc(Long videoId, String targetLang);
    List<DubJob> findAllByVideoIdOrderByCreatedAtDesc(Long videoId);
}