import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Video } from '../../models/video.model';
import { VideoService, PageResponse } from '../../services/video.service';
import { DubService } from '../../services/dub.service';
import { DubJob } from '../../models/dub-job.model';
import { VideoPlayerComponent } from '../video-player/video-player.component';
import { UploadDialogComponent } from '../upload-dialog/upload-dialog.component';

@Component({
  selector: 'app-video-list',
  standalone: true,
  imports: [CommonModule, FormsModule, VideoPlayerComponent, UploadDialogComponent],
  templateUrl: './video-list.component.html',
  styleUrl: './video-list.component.css'
})
export class VideoListComponent implements OnInit, OnDestroy {

  videos: Video[] = [];
  currentVideo: Video | null = null;
  showPlayer = false;
  showUpload = false;
  searchQuery = '';
  page = 0;
  size = 12;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  sortOrder: string = 'desc';
  sortBy: string = 'createdAt';

  dubJobs: { [videoId: number]: DubJob } = {};
  dubLoading: { [videoId: number]: boolean } = {};
  private dubTimers: { [videoId: number]: any } = {};

  constructor(
    private videoService: VideoService,
    private dubService: DubService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadVideos();
  }

  loadVideos(page: number = this.page): void {
    this.loading = true;
    this.videoService.getAllVideos(page, this.size).subscribe({
      next: (res: PageResponse<Video>) => {
        this.videos = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.page = res.number;
        this.loading = false;
        this.preloadDubStatus();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des vidéos', err);
        this.loading = false;
      }
    });
  }

  private preloadDubStatus(): void {
    this.videos.forEach(video => {
      if (this.dubLoading[video.id]) return;
      this.dubService.getJobForVideo(video.id, 'fr').subscribe({
        next: (job) => {
          if (job) {
            this.dubJobs = { ...this.dubJobs, [video.id]: job };
            this.cdr.detectChanges();
            if (job.status === 'PENDING' || job.status === 'PROCESSING' || job.status === 'STREAMING') {
              this.startDubPolling(video.id);
            }
          } else {
            // Lancement automatique du doublage audio track en tâche de fond
            this.dubService.generate(video.id, 'fr').subscribe({
              next: (newJob) => {
                this.dubJobs = { ...this.dubJobs, [video.id]: newJob };
                this.cdr.detectChanges();
                this.startDubPolling(video.id);
              },
              error: () => {}
            });
          }
        },
        error: () => {}
      });
    });
  }

  generateDub(video: Video, event: Event, force: boolean = false): void {
    event.stopPropagation();
    const existing = this.dubJobs[video.id];
    if (this.dubLoading[video.id]) return;
    if (!force && existing && (existing.status === 'PENDING' || existing.status === 'PROCESSING')) return;
    if (existing && (existing.status === 'READY' || existing.status === 'STREAMING' || existing.status === 'ERROR' || force)) {
      const copy = { ...this.dubJobs };
      delete copy[video.id];
      this.dubJobs = copy;
      this.cdr.detectChanges();
    }
    this.dubLoading[video.id] = true;
    this.dubService.generate(video.id, 'fr', force || (existing?.status === 'ERROR')).subscribe({
      next: (job) => {
        this.dubLoading[video.id] = false;
        this.dubJobs = { ...this.dubJobs, [video.id]: job };
        this.cdr.detectChanges();
        this.startDubPolling(video.id);
      },
      error: (err) => {
        console.error('Erreur lors du lancement du doublage', err);
        this.dubLoading[video.id] = false;
      }
    });
  }

  cancelDub(video: Video, event: Event): void {
    event.stopPropagation();
    if (this.dubTimers[video.id]) {
      clearInterval(this.dubTimers[video.id]);
      delete this.dubTimers[video.id];
    }
    const copy = { ...this.dubJobs };
    delete copy[video.id];
    this.dubJobs = copy;
    this.cdr.detectChanges();
    this.dubService.deleteForVideo(video.id).subscribe({
      next: () => {},
      error: () => {}
    });
  }

  private startDubPolling(videoId: number): void {
    if (this.dubTimers[videoId]) clearInterval(this.dubTimers[videoId]);
    this.dubTimers[videoId] = setInterval(() => {
      this.dubService.getJobForVideo(videoId, 'fr').subscribe({
        next: (job) => {
          if (!job) return;
          this.dubJobs = { ...this.dubJobs, [videoId]: job };
          this.cdr.detectChanges();
          if (job.status === 'READY' || job.status === 'ERROR') {
            clearInterval(this.dubTimers[videoId]);
            delete this.dubTimers[videoId];
          }
        },
        error: (err) => {
          console.warn(`Polling error on video ${videoId}`, err);
        }
      });
    }, 2000);
  }

  playVideoWithTrack(video: Video, track: string, event: Event): void {
    event.stopPropagation();
    try {
      localStorage.setItem('vp-dub-track:' + video.id, track);
    } catch (e) {}
    this.playVideo(video);
  }

  search(): void {
    if (this.searchQuery.trim()) {
      this.loading = true;
      this.videoService.searchVideos(this.searchQuery.trim(), 0, this.size).subscribe({
        next: (res: PageResponse<Video>) => {
          this.videos = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.page = 0;
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur lors de la recherche', err);
          this.loading = false;
        }
      });
    } else {
      this.loadVideos(0);
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadVideos(0);
  }

  changePage(delta: number): void {
    const newPage = this.page + delta;
    if (newPage >= 0 && newPage < this.totalPages) {
      this.loadVideos(newPage);
    }
  }

  playVideo(video: Video): void {
    this.currentVideo = video;
    this.showPlayer = true;
    try {
      window.scrollTo({ top: 0, behavior: 'instant' });
    } catch (e) {}
  }

  closePlayer(): void {
    this.showPlayer = false;
    this.currentVideo = null;
    try {
      window.scrollTo({ top: 0, behavior: 'instant' });
    } catch (e) {}
    this.refreshList();
  }

  onSelectVideo(video: Video): void {
    this.currentVideo = video;
  }

  onPlayerVideoEnded(): void {
    if (!this.currentVideo) return;
    const index = this.videos.findIndex(v => v.id === this.currentVideo?.id);
    if (index >= 0 && index < this.videos.length - 1) {
      this.currentVideo = this.videos[index + 1];
    }
  }

  deleteVideo(video: Video, event: Event): void {
    event.stopPropagation();
    if (confirm(`Voulez-vous supprimer la vidéo "${video.title}" ?`)) {
      this.videoService.deleteVideo(video.id).subscribe({
        next: () => {
          this.refreshList();
          if (this.currentVideo?.id === video.id) {
            this.currentVideo = null;
            this.showPlayer = false;
          }
        },
        error: (err) => console.error('Erreur lors de la suppression', err)
      });
    }
  }

  onUploadComplete(): void {
    this.showUpload = false;
    this.refreshList();
  }

  onUploadClosed(): void {
    this.showUpload = false;
  }

  refreshList(): void {
    this.loadVideos(this.page);
  }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`;
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  ngOnDestroy(): void {
    for (const id in this.dubTimers) {
      clearInterval(this.dubTimers[id]);
    }
  }
}
