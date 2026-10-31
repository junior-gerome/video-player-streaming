package com.videoplayer.repository;

import com.videoplayer.model.DubSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DubSegmentRepository extends JpaRepository<DubSegment, Long> {

    List<DubSegment> findByVideoIdAndTargetLangOrderBySegmentIndexAsc(Long videoId, String targetLang);

    Optional<DubSegment> findByVideoIdAndTargetLangAndSegmentIndex(Long videoId, String targetLang, Integer segmentIndex);

    boolean existsByVideoIdAndTargetLang(Long videoId, String targetLang);

    @Transactional
    void deleteByVideoIdAndTargetLang(Long videoId, String targetLang);

    @Transactional
    void deleteByVideoId(Long videoId);
}
