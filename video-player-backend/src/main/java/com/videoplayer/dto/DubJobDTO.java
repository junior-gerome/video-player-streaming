package com.videoplayer.dto;

import java.time.LocalDateTime;

public class DubJobDTO {
    private Long id;
    private Long videoId;
    private String videoTitle;
    private String targetLang;
    private String status;
    private Integer progress;
    private String message;
    private String error;
    private String audioUrl;
    private Boolean cloneVoice = false;
    private Boolean demucs = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DubJobDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Boolean getCloneVoice() { return cloneVoice != null ? cloneVoice : false; }
    public void setCloneVoice(Boolean cloneVoice) { this.cloneVoice = cloneVoice; }

    public Boolean getDemucs() { return demucs != null ? demucs : true; }
    public void setDemucs(Boolean demucs) { this.demucs = demucs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}