import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Playlist, LoopMode } from '../../models/playlist.model';
import { Video } from '../../models/video.model';
import { PlaylistService } from '../../services/playlist.service';
import { VideoService, PageResponse } from '../../services/video.service';
import { VideoPlayerComponent } from '../video-player/video-player.component';

@Component({
  selector: 'app-playlist-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, VideoPlayerComponent],
  templateUrl: './playlist-manager.component.html',
  styleUrl: './playlist-manager.component.css'
})
export class PlaylistManagerComponent implements OnInit {

  readonly LOOP_MODES = ['none', 'one', 'all'] as const;
  readonly LOOP_LABELS: Record<string, string> = {
    none: 'Boucle désactivée',
    one: 'Répéter la vidéo (boucle vidéo unique)',
    all: 'Répéter toute la playlist'
  };

  playlists: Playlist[] = [];
  allVideos: Video[] = [];
  selectedPlaylist: Playlist | null = null;
  showCreateForm = false;
  newName = '';
  newDescription = '';
  showAddVideoModal = false;
  selectedVideoIds: Set<number> = new Set();
  currentVideo: Video | null = null;
  showPlayer = false;
  loading = false;

  constructor(
    private playlistService: PlaylistService,
    private videoService: VideoService
  ) {}

  ngOnInit(): void {
    this.loadPlaylists();
    this.loadAllVideos();
  }

  loadPlaylists(): void {
    this.loading = true;
    this.playlistService.getAllPlaylists().subscribe({
      next: (playlists) => {
        this.playlists = playlists;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des playlists', err);
        this.loading = false;
      }
    });
  }

  loadAllVideos(): void {
    this.videoService.getAllVideos(0, 100).subscribe({
      next: (res: PageResponse<Video>) => {
        this.allVideos = res.content;
      },
      error: (err) => console.error('Erreur lors du chargement des vidéos', err)
    });
  }

  createPlaylist(): void {
    if (!this.newName.trim()) return;
    this.playlistService.createPlaylist(this.newName.trim(), this.newDescription.trim())
      .subscribe({
        next: (playlist) => {
          this.playlists.push(playlist);
          this.showCreateForm = false;
          this.newName = '';
          this.newDescription = '';
          this.selectPlaylist(playlist);
        },
        error: (err) => console.error('Erreur lors de la création', err)
      });
  }

  selectPlaylist(playlist: Playlist): void {
    if (this.selectedPlaylist?.id !== playlist.id) {
      this.playlistService.getPlaylistById(playlist.id).subscribe({
        next: (fullPlaylist) => {
          this.selectedPlaylist = fullPlaylist;
        },
        error: (err) => console.error('Erreur', err)
      });
    }
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
  }

  deletePlaylist(playlist: Playlist, event: Event): void {
    event.stopPropagation();
    if (confirm(`Voulez-vous supprimer la playlist "${playlist.name}" ?`)) {
      this.playlistService.deletePlaylist(playlist.id).subscribe({
        next: () => {
          this.playlists = this.playlists.filter(p => p.id !== playlist.id);
          if (this.selectedPlaylist?.id === playlist.id) {
            this.selectedPlaylist = null;
          }
        },
        error: (err) => console.error('Erreur lors de la suppression', err)
      });
    }
  }

  openAddVideoModal(): void {
    this.selectedVideoIds = new Set();
    if (this.selectedPlaylist) {
      this.selectedPlaylist.videos.forEach(v => this.selectedVideoIds.add(v.id));
    }
    this.showAddVideoModal = true;
  }

  toggleVideoSelection(videoId: number): void {
    if (this.selectedVideoIds.has(videoId)) {
      this.selectedVideoIds.delete(videoId);
    } else {
      this.selectedVideoIds.add(videoId);
    }
  }

  isVideoSelected(videoId: number): boolean {
    return this.selectedVideoIds.has(videoId);
  }

  addSelectedVideos(): void {
    if (!this.selectedPlaylist) return;
    const videoIds = Array.from(this.selectedVideoIds);
    this.playlistService.addVideosToPlaylist(this.selectedPlaylist.id, videoIds)
      .subscribe({
        next: (playlist) => {
          this.selectedPlaylist = playlist;
          this.showAddVideoModal = false;
          this.refreshPlaylistInList(playlist);
        },
        error: (err) => console.error('Erreur lors de l\'ajout', err)
      });
  }

  removeVideoFromPlaylist(videoId: number): void {
    if (!this.selectedPlaylist) return;
    this.playlistService.removeVideoFromPlaylist(this.selectedPlaylist.id, videoId)
      .subscribe({
        next: (playlist) => {
          this.selectedPlaylist = playlist;
          this.refreshPlaylistInList(playlist);
        },
        error: (err) => console.error('Erreur lors du retrait', err)
      });
  }

  refreshPlaylistInList(updated: Playlist): void {
    const index = this.playlists.findIndex(p => p.id === updated.id);
    if (index !== -1) {
      this.playlists[index] = updated;
    }
  }

  playFromPlaylist(video: Video): void {
    if (!this.selectedPlaylist) return;
    this.currentVideo = video;
    this.showPlayer = true;
  }

  onSelectVideo(video: Video): void {
    this.currentVideo = video;
  }

  onVideoEnded(): void {
    if (!this.selectedPlaylist) return;
    const idx = this.selectedPlaylist.videos.findIndex(v => v.id === this.currentVideo?.id);
    if (idx >= 0 && idx < this.selectedPlaylist.videos.length - 1) {
      this.currentVideo = this.selectedPlaylist.videos[idx + 1];
    }
  }

  cyclePlaylistLoopMode(): void {
    if (!this.selectedPlaylist) return;
    const idx = this.LOOP_MODES.indexOf(this.selectedPlaylist.loopMode);
    const next = this.LOOP_MODES[(idx + 1) % this.LOOP_MODES.length];
    this.setPlaylistLoopMode(next);
  }

  onPlayerLoopModeChange(mode: LoopMode): void {
    if (this.selectedPlaylist && this.selectedPlaylist.loopMode !== mode) {
      this.setPlaylistLoopMode(mode);
    }
  }

  private setPlaylistLoopMode(mode: LoopMode): void {
    if (!this.selectedPlaylist) return;
    const playlistId = this.selectedPlaylist.id;
    this.selectedPlaylist = { ...this.selectedPlaylist, loopMode: mode };
    this.playlistService.setLoopMode(playlistId, mode).subscribe({
      next: (playlist) => {
        this.selectedPlaylist = playlist;
        this.refreshPlaylistInList(playlist);
      },
      error: (err) => console.error('Erreur lors de la mise à jour du mode de boucle', err)
    });
  }

  closePlayer(): void {
    this.showPlayer = false;
    this.currentVideo = null;
  }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`;
  }

  getVideoNumber(videoId: number): number {
    if (!this.selectedPlaylist) return 0;
    const idx = this.selectedPlaylist.videos.findIndex(v => v.id === videoId);
    return idx >= 0 ? idx + 1 : 0;
  }
}
