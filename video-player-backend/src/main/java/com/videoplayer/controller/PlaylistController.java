package com.videoplayer.controller;

import com.videoplayer.dto.PlaylistDTO;
import com.videoplayer.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.getOrDefault("description", "");
        String loopMode = request.getOrDefault("loopMode", "none");
        PlaylistDTO playlist = playlistService.createPlaylist(name, description, loopMode);
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    @GetMapping
    public ResponseEntity<List<PlaylistDTO>> getAllPlaylists() {
        return ResponseEntity.ok(playlistService.getAllPlaylists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistDTO> updatePlaylist(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.getOrDefault("description", "");
        String loopMode = request.getOrDefault("loopMode", "none");
        return ResponseEntity.ok(playlistService.updatePlaylist(id, name, description, loopMode));
    }

    @PutMapping("/{id}/loop-mode")
    public ResponseEntity<PlaylistDTO> updateLoopMode(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String loopMode = request.getOrDefault("loopMode", "none");
        return ResponseEntity.ok(playlistService.updateLoopMode(id, loopMode));
    }

    @PutMapping("/{id}/videos")
    public ResponseEntity<PlaylistDTO> addVideosToPlaylist(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> request) {
        List<Long> videoIds = request.get("videoIds");
        return ResponseEntity.ok(playlistService.addVideosToPlaylist(id, videoIds));
    }

    @DeleteMapping("/{id}/videos/{videoId}")
    public ResponseEntity<PlaylistDTO> removeVideoFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long videoId) {
        return ResponseEntity.ok(playlistService.removeVideoFromPlaylist(id, videoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}
