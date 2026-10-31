package com.videoplayer.service;

import com.videoplayer.dto.PlaylistDTO;
import com.videoplayer.dto.VideoDTO;
import com.videoplayer.model.Playlist;
import com.videoplayer.model.PlaylistVideo;
import com.videoplayer.model.Video;
import com.videoplayer.repository.PlaylistRepository;
import com.videoplayer.repository.PlaylistVideoRepository;
import com.videoplayer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistVideoRepository playlistVideoRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final Set<String> VALID_LOOP_MODES = Set.of("none", "one", "all");

    public PlaylistDTO createPlaylist(String name, String description, String loopMode) {
        Playlist playlist = new Playlist(name, description);
        playlist.setLoopMode(normalizeLoopMode(loopMode));
        playlist = playlistRepository.save(playlist);
        return toDTO(playlist);
    }

    public List<PlaylistDTO> getAllPlaylists() {
        return playlistRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PlaylistDTO getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée avec l'id: " + id));
        return toDTO(playlist);
    }

    @Transactional
    public PlaylistDTO addVideosToPlaylist(Long playlistId, List<Long> videoIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée avec l'id: " + playlistId));

        int currentMaxPosition = playlistVideoRepository.findByPlaylistIdOrderByPositionAsc(playlistId).size();

        for (Long videoId : videoIds) {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Vidéo non trouvée avec l'id: " + videoId));

            boolean alreadyExists = playlistVideoRepository.findByPlaylistIdOrderByPositionAsc(playlistId)
                    .stream()
                    .anyMatch(pv -> pv.getVideo().getId().equals(videoId));

            if (!alreadyExists) {
                PlaylistVideo pv = new PlaylistVideo(playlist, video, currentMaxPosition++);
                playlistVideoRepository.save(pv);
            }
        }

        return getPlaylistById(playlistId);
    }

    @Transactional
    public PlaylistDTO removeVideoFromPlaylist(Long playlistId, Long videoId) {
        playlistVideoRepository.deleteByPlaylistIdAndVideoId(playlistId, videoId);
        return getPlaylistById(playlistId);
    }

    public void deletePlaylist(Long id) {
        playlistRepository.deleteById(id);
    }

    public PlaylistDTO updatePlaylist(Long id, String name, String description, String loopMode) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée avec l'id: " + id));
        playlist.setName(name);
        playlist.setDescription(description);
        playlist.setLoopMode(normalizeLoopMode(loopMode));
        playlist = playlistRepository.save(playlist);
        return toDTO(playlist);
    }

    public PlaylistDTO updateLoopMode(Long id, String loopMode) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée avec l'id: " + id));
        playlist.setLoopMode(normalizeLoopMode(loopMode));
        playlist = playlistRepository.save(playlist);
        return toDTO(playlist);
    }

    private String normalizeLoopMode(String loopMode) {
        if (loopMode == null || loopMode.isBlank()) return "none";
        return VALID_LOOP_MODES.contains(loopMode) ? loopMode : "none";
    }

    private PlaylistDTO toDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setLoopMode(playlist.getLoopMode());
        dto.setCreatedAt(playlist.getCreatedAt());

        List<PlaylistVideo> pvs = playlistVideoRepository.findByPlaylistIdOrderByPositionAsc(playlist.getId());
        List<VideoDTO> videos = pvs.stream()
                .map(pv -> toVideoDTO(pv.getVideo()))
                .collect(Collectors.toList());

        dto.setVideos(videos);
        dto.setVideoCount(videos.size());
        return dto;
    }

    private VideoDTO toVideoDTO(Video video) {
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
