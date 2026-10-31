package com.videoplayer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dub_segments", indexes = {
    @Index(name = "idx_dub_seg_video_lang", columnList = "videoId, targetLang"),
    @Index(name = "idx_dub_seg_lookup", columnList = "videoId, targetLang, segmentIndex")
})
public class DubSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false, length = 10)
    private String targetLang;

    private String voice;

    @Column(nullable = false)
    private Integer segmentIndex;

    @Column(nullable = false)
    private Double startTime;

    @Column(nullable = false)
    private Double endTime;

    @Column(length = 2000)
    private String originalText;

    @Column(length = 2000)
    private String translatedText;

    private String audioObjectName;

    private String audioUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DubSegment() {}

    public DubSegment(Long videoId, String targetLang, String voice, Integer segmentIndex,
                      Double startTime, Double endTime, String audioObjectName, String audioUrl) {
        this.videoId = videoId;
        this.targetLang = targetLang;
        this.voice = voice;
        this.segmentIndex = segmentIndex;
        this.startTime = startTime;
        this.endTime = endTime;
        this.audioObjectName = audioObjectName;
        this.audioUrl = audioUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }

    public Integer getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(Integer segmentIndex) { this.segmentIndex = segmentIndex; }

    public Double getStartTime() { return startTime; }
    public void setStartTime(Double startTime) { this.startTime = startTime; }

    public Double getEndTime() { return endTime; }
    public void setEndTime(Double endTime) { this.endTime = endTime; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getAudioObjectName() { return audioObjectName; }
    public void setAudioObjectName(String audioObjectName) { this.audioObjectName = audioObjectName; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
