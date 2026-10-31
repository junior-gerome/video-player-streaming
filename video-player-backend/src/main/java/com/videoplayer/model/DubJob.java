package com.videoplayer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dub_jobs")
public class DubJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long videoId;

    private String videoTitle;

    @Column(nullable = false)
    private String status;

    private Integer progress = 0;

    @Column(length = 1000)
    private String message;

    private String targetLang = "fr";

    private Boolean cloneVoice = false;

    private Boolean demucs = true;

    private String audioObjectName;

    @Column(length = 2000)
    private String error;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public DubJob() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

    public Boolean getCloneVoice() { return cloneVoice != null ? cloneVoice : false; }
    public void setCloneVoice(Boolean cloneVoice) { this.cloneVoice = cloneVoice; }

    public Boolean getDemucs() { return demucs != null ? demucs : true; }
    public void setDemucs(Boolean demucs) { this.demucs = demucs; }

    public String getAudioObjectName() { return audioObjectName; }
    public void setAudioObjectName(String audioObjectName) { this.audioObjectName = audioObjectName; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}