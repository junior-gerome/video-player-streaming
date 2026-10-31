package com.videoplayer.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private String loopMode;
    private LocalDateTime createdAt;
    private List<VideoDTO> videos;
    private Integer videoCount;

    public PlaylistDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLoopMode() { return loopMode; }
    public void setLoopMode(String loopMode) { this.loopMode = loopMode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<VideoDTO> getVideos() { return videos; }
    public void setVideos(List<VideoDTO> videos) { this.videos = videos; }

    public Integer getVideoCount() { return videoCount; }
    public void setVideoCount(Integer videoCount) { this.videoCount = videoCount; }
}
