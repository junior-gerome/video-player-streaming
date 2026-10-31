package com.videoplayer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_videos")
public class PlaylistVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private Integer position = 0;

    public PlaylistVideo() {}

    public PlaylistVideo(Playlist playlist, Video video, Integer position) {
        this.playlist = playlist;
        this.video = video;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
