import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, ElementRef, ViewChild, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Video } from '../../models/video.model';
import { VideoService } from '../../services/video.service';
import { DubService, SUPPORTED_LANGUAGES, SupportedLanguage, ArgosModel } from '../../services/dub.service';
import { DubWebSocketService } from '../../services/dub-websocket.service';
import { DubJob, DubSegment } from '../../models/dub-job.model';

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './video-player.component.html',
  styleUrl: './video-player.component.css'
})
export class VideoPlayerComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {

  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;
  @ViewChild('dubAudioElement') dubAudioElement!: ElementRef<HTMLAudioElement>;
  @ViewChild('segmentAudioElement') segmentAudioElement!: ElementRef<HTMLAudioElement>;

  @Input() videos: Video[] = [];
  @Input() currentVideo: Video | null = null;
  @Input() loopModeFromParent: 'none' | 'one' | 'all' | null = null;
  @Output() videoEnded = new EventEmitter<void>();
  @Output() selectVideo = new EventEmitter<Video>();
  @Output() loopModeChange = new EventEmitter<'none' | 'one' | 'all'>();
  @Output() close = new EventEmitter<void>();

  isPlaying = false;
  currentTime = 0;
  duration = 0;
  volume = 1;
  muted = false;
  isMuted = false;
  showControls = true;
  controlTimeout: any;
  isFullscreen = false;
  playbackRate = 1;
  showSpeedMenu = false;
  loopMode: 'none' | 'one' | 'all' = 'none';
  readonly LOOP_MODES = ['none', 'one', 'all'] as const;
  readonly LOOP_LABELS: Record<string, string> = {
    none: 'Boucle désactivée',
    one: 'Répéter la vidéo (boucle vidéo unique)',
    all: 'Répéter toute la playlist'
  };

  private static readonly STALL_RESYNC_DELAY = 4000;
  private static readonly SPINNER_DELAY = 600;
  private static readonly CAPTURE_EVERY_MS = 1000;

  isBuffering = false;
  bufferingVisible = false;
  isResyncing = false;
  bufferEnd = 0;
  playerError = false;
  posterImage: string | null = null;
  private bufferingTimer: any;
  private stallTimer: any;
  private pendingSeek: number | null = null;
  private resyncWasPlaying = false;
  private resyncAttempts = 0;
  private lastCapture = 0;

  supportedLanguages: SupportedLanguage[] = SUPPORTED_LANGUAGES;
  dubJobs: Map<string, DubJob> = new Map();
  generatingLangs: Map<string, { progress: number; message: string }> = new Map();
  activeTrack = 'vo';
  dubActive = false;
  dubUrl: string | null = null;
  showAudioMenu = false;
  liveSegmentsList: DubSegment[] = [];
  playingSegmentIndex: number | null = null;
  segmentsCountByLang: Map<string, number> = new Map();

  // Nouvelles fonctionnalités avancées
  demucsEnabled = true;       // Préservation de la musique de fond
  cloneVoiceEnabled = false;  // Clonage de voix Zero-Shot

  // Pré-téléchargement des paquets hors-connexion Argos Translate
  showArgosModal = false;
  argosModels: ArgosModel[] = [];
  isDownloadingArgosAll = false;
  private argosPollTimer: any = null;

  private wsSubscriptions: Subscription[] = [];
  private static readonly DUB_PREF_KEY = 'vp-dub-track:';

  constructor(
    private videoService: VideoService,
    private dubService: DubService,
    private dubWs: DubWebSocketService
  ) {}

  ngOnInit(): void {
    this.initWebSocketListeners();
  }

  private initWebSocketListeners(): void {
    // 1. Réception des répliques doublées en streaming direct via WebSocket
    this.wsSubscriptions.push(
      this.dubWs.segment$.subscribe(seg => {
        this.addOrUpdateSegment(this.activeTrack, seg);
      })
    );

    // 2. Réception du lot de segments existants (cache base de données)
    this.wsSubscriptions.push(
      this.dubWs.segmentsBatch$.subscribe(segs => {
        for (const s of segs) {
          this.addOrUpdateSegment(this.activeTrack, s);
        }
      })
    );

    // 3. Mises à jour d'état du job
    this.wsSubscriptions.push(
      this.dubWs.job$.subscribe(job => {
        if (!this.currentVideo || this.currentVideo.id !== job.videoId) return;
        this.dubJobs.set(job.targetLang, job);

        if (job.status === 'READY') {
          if (this.activeTrack === job.targetLang) {
            this.dubUrl = this.dubService.getAudioUrl(job.videoId, job.targetLang);
            this.activateDubSeamless();
          }
          this.generatingLangs.delete(job.targetLang);
        } else if (job.status === 'ERROR') {
          this.generatingLangs.delete(job.targetLang);
        } else {
          this.generatingLangs.set(job.targetLang, { progress: job.progress, message: job.message });
        }
      })
    );

    // 4. Progression continue
    this.wsSubscriptions.push(
      this.dubWs.progress$.subscribe(p => {
        if (this.activeTrack !== 'vo') {
          this.generatingLangs.set(this.activeTrack, { progress: p.progress, message: p.message });
        }
      })
    );

    // 5. Bascule automatique quand la piste complète est finalisée
    this.wsSubscriptions.push(
      this.dubWs.audioReady$.subscribe(audioUrl => {
        if (this.activeTrack !== 'vo') {
          this.dubUrl = audioUrl;
          this.activateDubSeamless();
        }
      })
    );
  }

  ngAfterViewInit(): void {
    if (this.currentVideo) {
      this.loadDubStatus();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentVideo'] && !changes['currentVideo'].firstChange) {
      const video = this.videoElement?.nativeElement;
      if (video && !video.paused) {
        video.pause();
      }
      this.isPlaying = false;
      this.currentTime = 0;
      this.duration = 0;
      this.isBuffering = false;
      this.bufferingVisible = false;
      this.isResyncing = false;
      this.bufferEnd = 0;
      this.posterImage = null;
      this.playerError = false;
      this.pendingSeek = null;
      this.resyncAttempts = 0;
      this.clearStallTimer();
      this.clearBufferingTimer();
      this.resetDub();
    }
    if (changes['loopModeFromParent']) {
      const parent = changes['loopModeFromParent'].currentValue;
      if (parent && parent !== this.loopMode) {
        this.loopMode = parent;
      }
    }
  }

  getActiveTrackLabel(): string {
    if (this.activeTrack === 'vo' || !this.dubActive) {
      return 'VO — Original';
    }
    const found = this.supportedLanguages.find(l => l.code === this.activeTrack);
    const label = found ? `${found.flag} ${found.label}` : this.activeTrack.toUpperCase();
    return this.cloneVoiceEnabled ? `🎙️ ${label}` : label;
  }

  isReady(langCode: string): boolean {
    const job = this.dubJobs.get(langCode);
    return job?.status === 'READY' || job?.status === 'STREAMING' || (this.segmentsCountByLang.get(langCode) ?? 0) > 0;
  }

  getSegmentCount(langCode: string): number {
    return this.segmentsCountByLang.get(langCode) ?? (this.activeTrack === langCode ? this.liveSegmentsList.length : 0);
  }

  isStreaming(langCode: string): boolean {
    const job = this.dubJobs.get(langCode);
    return job?.status === 'STREAMING';
  }

  isGenerating(langCode: string): boolean {
    return this.generatingLangs.has(langCode);
  }

  getGenerationProgress(langCode: string): number {
    return this.generatingLangs.get(langCode)?.progress ?? 0;
  }

  getGenerationMessage(langCode: string): string {
    return this.generatingLangs.get(langCode)?.message ?? 'En cours...';
  }

  private resetDub(): void {
    this.deactivateDub();
    this.stopSegmentAudio();
    this.liveSegmentsList = [];
    this.segmentsCountByLang.clear();
    this.dubJobs.clear();
    this.generatingLangs.clear();
    this.dubUrl = null;
    this.activeTrack = 'vo';
    this.showAudioMenu = false;
    this.loadDubStatus();
  }

  private loadDubStatus(): void {
    const video = this.currentVideo;
    if (!video) return;

    this.dubService.getAllTracksForVideo(video.id).subscribe({
      next: (jobs) => {
        if (!this.currentVideo || this.currentVideo.id !== video.id) return;
        this.dubJobs.clear();

        for (const job of jobs) {
          this.dubJobs.set(job.targetLang, job);
          this.dubService.getSegments(video.id, job.targetLang).subscribe({
            next: (segs) => {
              this.segmentsCountByLang.set(job.targetLang, segs.length);
              if (this.activeTrack === job.targetLang) {
                this.liveSegmentsList = segs;
                this.syncDubAudio();
              }
            }
          });

          if (job.status === 'PROCESSING' || job.status === 'PENDING' || job.status === 'STREAMING') {
            this.generatingLangs.set(job.targetLang, { progress: job.progress, message: job.message });
            this.dubWs.subscribe(video.id, job.targetLang);
          }
        }

        let preferredTrack: string | null = null;
        try {
          preferredTrack = localStorage.getItem(VideoPlayerComponent.DUB_PREF_KEY + video.id);
        } catch (e) {}

        if (preferredTrack && preferredTrack !== 'vo') {
          this.selectAudioTrack(preferredTrack);
        } else if (this.isReady('fr')) {
          this.selectAudioTrack('fr');
        }
      },
      error: () => {}
    });
  }

  selectAudioTrack(track: string): void {
    this.showAudioMenu = false;
    const video = this.videoElement?.nativeElement;

    if (track === 'vo') {
      this.activeTrack = 'vo';
      this.deactivateDub();
      try {
        localStorage.setItem(VideoPlayerComponent.DUB_PREF_KEY + (this.currentVideo?.id ?? ''), 'vo');
      } catch (e) {}
      return;
    }

    this.activeTrack = track;

    // Démarrage instantané de la vidéo si elle était en pause (délai perçu quasi-nul)
    if (video && video.paused) {
      video.play().then(() => {
        this.isPlaying = true;
        this.isBuffering = false;
      }).catch(() => {
        this.isPlaying = !video.paused;
      });
    }

    const job = this.dubJobs.get(track);

    if (job?.status === 'READY') {
      this.dubUrl = this.dubService.getAudioUrl(this.currentVideo!.id, track);
      this.activateDubSeamless();
    } else {
      // Mode flux temps réel par répliques / segments avec fond musical
      this.deactivateFullDub();
      this.dubActive = true;
      this.stopSegmentAudio();
      this.liveSegmentsList = [];
      this.playingSegmentIndex = null;
      this.fetchSegmentsForActiveTrack();

      if (this.currentVideo) {
        this.dubWs.subscribe(this.currentVideo.id, track);
        this.dubWs.startDub(this.currentVideo.id, track, {
          cloneVoice: this.cloneVoiceEnabled,
          demucs: this.demucsEnabled,
          force: false
        });
        this.generatingLangs.set(track, { progress: 0, message: 'Démarrage du streaming en direct...' });

        // Repli automatique HTTP si le WebSocket n'est pas encore actif
        if (!this.dubWs.isSocketOpen) {
          this.dubService.generate(this.currentVideo.id, track, false, this.cloneVoiceEnabled, this.demucsEnabled).subscribe({
            next: (job) => {
              this.dubJobs.set(track, job);
              if (job.status === 'READY') {
                this.dubUrl = this.dubService.getAudioUrl(this.currentVideo!.id, track);
                this.activateDubSeamless();
              }
            },
            error: (err) => console.warn('Repli HTTP doublage:', err)
          });
        }
      }
    }

    try {
      localStorage.setItem(VideoPlayerComponent.DUB_PREF_KEY + (this.currentVideo?.id ?? ''), track);
    } catch (e) {}
  }

  private fetchSegmentsForActiveTrack(): void {
    const video = this.currentVideo;
    if (!video || this.activeTrack === 'vo') return;

    this.dubService.getSegments(video.id, this.activeTrack).subscribe({
      next: (segs) => {
        this.liveSegmentsList = segs;
        this.segmentsCountByLang.set(this.activeTrack, segs.length);
        this.syncDubAudio();
      },
      error: () => {}
    });
  }

  private addOrUpdateSegment(langCode: string, seg: DubSegment): void {
    if (this.activeTrack === langCode) {
      const idx = this.liveSegmentsList.findIndex(s => s.index === seg.index);
      if (idx >= 0) {
        this.liveSegmentsList[idx] = seg;
      } else {
        this.liveSegmentsList.push(seg);
        this.liveSegmentsList.sort((a, b) => a.start - b.start);
      }
      this.segmentsCountByLang.set(langCode, this.liveSegmentsList.length);
      this.syncDubAudio();
    } else {
      const curCount = this.segmentsCountByLang.get(langCode) ?? 0;
      this.segmentsCountByLang.set(langCode, curCount + 1);
    }
  }

  toggleDubAudio(): void {
    if (this.dubActive) {
      this.deactivateDub();
    } else if (this.activeTrack !== 'vo') {
      this.selectAudioTrack(this.activeTrack);
    }
  }

  // Bascule transparente vers la piste complète sans coupure audible ni saut
  private activateDubSeamless(): void {
    const dub = this.dubAudioElement?.nativeElement;
    const video = this.videoElement?.nativeElement;
    if (!dub || !this.dubUrl || !video) return;

    this.dubActive = true;
    dub.src = this.dubUrl;
    dub.preload = 'auto';
    dub.playbackRate = this.playbackRate;
    dub.volume = this.volume;
    dub.muted = this.isMuted;

    let switched = false;
    const executeSwitch = () => {
      if (switched) return;
      switched = true;
      try {
        dub.currentTime = video.currentTime;
      } catch (e) {}

      if (!video.paused || this.isPlaying) {
        dub.play().then(() => {
          this.stopSegmentAudio();
          video.muted = true;
        }).catch(() => {
          this.stopSegmentAudio();
          video.muted = true;
        });
      } else {
        this.stopSegmentAudio();
        video.muted = true;
      }
    };

    dub.addEventListener('canplay', executeSwitch, { once: true });
    dub.load();
    if (dub.readyState >= 3) {
      executeSwitch();
    }
  }

  private deactivateFullDub(): void {
    const dub = this.dubAudioElement?.nativeElement;
    if (dub) {
      dub.pause();
      dub.removeAttribute('src');
      dub.load();
    }
  }

  private stopSegmentAudio(): void {
    const segAudio = this.segmentAudioElement?.nativeElement;
    if (segAudio) {
      segAudio.pause();
      segAudio.removeAttribute('src');
    }
    this.playingSegmentIndex = null;
  }

  private deactivateDub(): void {
    this.deactivateFullDub();
    this.stopSegmentAudio();
    this.dubActive = false;
    const video = this.videoElement?.nativeElement;
    if (video) {
      video.muted = this.isMuted;
      video.volume = this.volume;
    }
  }

  private syncDubAudio(): void {
    const video = this.videoElement?.nativeElement;
    if (!video) return;

    if (!this.dubActive || this.activeTrack === 'vo') {
      this.stopSegmentAudio();
      return;
    }

    const job = this.dubJobs.get(this.activeTrack);

    // 1. Mode continu complet prêt
    if (job?.status === 'READY') {
      this.stopSegmentAudio();
      const dub = this.dubAudioElement?.nativeElement;
      if (!dub || !dub.src) return;

      video.muted = true;
      if (dub.readyState >= 2 && Math.abs(dub.currentTime - video.currentTime) > 0.2) {
        dub.currentTime = video.currentTime;
      }
      if (!video.paused && dub.paused && dub.readyState >= 2) {
        dub.play().catch(err => console.warn('Dub play sync error', err));
      } else if (video.paused && !dub.paused) {
        dub.pause();
      }
      return;
    }

    // 2. Mode direct en temps réel par segments synchronisés
    const segAudio = this.segmentAudioElement?.nativeElement;
    if (!segAudio) return;

    if (video.paused) {
      if (!segAudio.paused) {
        segAudio.pause();
      }
      return;
    }

    const currentSeg = this.liveSegmentsList.find(s => this.currentTime >= s.start && this.currentTime <= s.end);

    if (currentSeg) {
      // Dialogue présent : la réplique streamée prend le relais
      video.muted = true;
      const segSrc = 'http://localhost:8080' + currentSeg.audioUrl;

      if (this.playingSegmentIndex !== currentSeg.index || segAudio.src !== segSrc) {
        this.playingSegmentIndex = currentSeg.index;
        segAudio.src = segSrc;
        segAudio.playbackRate = this.playbackRate;
        segAudio.muted = this.isMuted;
        segAudio.volume = this.volume;

        const offset = Math.max(0, this.currentTime - currentSeg.start);
        if (offset > 0.05) {
          const applyOffset = () => {
            try {
              segAudio.currentTime = Math.max(0, this.currentTime - currentSeg.start);
            } catch (e) {}
          };
          if (segAudio.readyState >= 1) {
            applyOffset();
          } else {
            segAudio.addEventListener('loadedmetadata', applyOffset, { once: true });
          }
        }

        if (!video.paused) {
          segAudio.play().catch(err => console.warn('Erreur lecture segment', err));
        }
        video.muted = true;
      } else {
        const expected = this.currentTime - currentSeg.start;
        if (segAudio.readyState >= 2 && Math.abs(segAudio.currentTime - expected) > 0.25) {
          segAudio.currentTime = expected;
        }
        if (segAudio.paused && !video.paused) {
          segAudio.play().catch(err => console.warn('Erreur reprise segment', err));
        }
        video.muted = true;
      }
    } else {
      if (this.playingSegmentIndex !== null) {
        this.playingSegmentIndex = null;
        segAudio.pause();
      }
      video.muted = this.isMuted;
      video.volume = this.volume;
    }
  }

  private dubSeekTo(time: number): void {
    const job = this.dubJobs.get(this.activeTrack);
    if (job?.status === 'READY') {
      const dub = this.dubAudioElement?.nativeElement;
      if (!this.dubActive || !dub || !dub.src) return;
      try {
        dub.currentTime = time;
      } catch (e) {
        console.warn('Dub seek error', e);
      }
    } else {
      // Envoi instantané de la commande SEEK prioritaire via le canal WebSocket bidirectionnel
      if (this.currentVideo && this.activeTrack !== 'vo') {
        this.dubWs.seek(this.currentVideo.id, this.activeTrack, time);
      }
      this.syncDubAudio();
    }
  }

  private dubPlay(): void {
    const job = this.dubJobs.get(this.activeTrack);
    if (job?.status === 'READY') {
      const dub = this.dubAudioElement?.nativeElement;
      const video = this.videoElement?.nativeElement;
      if (this.dubActive && dub && dub.src) {
        if (video && Math.abs(dub.currentTime - video.currentTime) > 0.2) {
          dub.currentTime = video.currentTime;
        }
        dub.play().catch(err => console.warn('Dub play error', err));
      }
    } else {
      this.syncDubAudio();
    }
  }

  private dubPause(): void {
    const dub = this.dubAudioElement?.nativeElement;
    if (this.dubActive && dub) {
      dub.pause();
    }
    this.stopSegmentAudio();
  }

  onDubAudioError(event: any): void {
    console.error('Erreur lors de la lecture de la piste audio doublée:', event);
  }

  ngOnDestroy(): void {
    const video = this.videoElement?.nativeElement;
    if (video && !video.paused) {
      video.pause();
    }
    this.wsSubscriptions.forEach(sub => sub.unsubscribe());
    this.clearStallTimer();
    this.clearBufferingTimer();
    if (this.argosPollTimer) {
      clearInterval(this.argosPollTimer);
      this.argosPollTimer = null;
    }
  }

  private clearStallTimer(): void {
    if (this.stallTimer) {
      clearTimeout(this.stallTimer);
      this.stallTimer = null;
    }
  }

  private clearBufferingTimer(): void {
    if (this.bufferingTimer) {
      clearTimeout(this.bufferingTimer);
      this.bufferingTimer = null;
    }
  }

  private showBuffering(): void {
    this.isBuffering = true;
    if (this.bufferingTimer) return;
    this.bufferingTimer = setTimeout(() => {
      this.bufferingTimer = null;
      this.bufferingVisible = true;
    }, VideoPlayerComponent.SPINNER_DELAY);
  }

  private hideBuffering(): void {
    this.isBuffering = false;
    this.bufferingVisible = false;
    this.clearBufferingTimer();
  }

  get currentIndex(): number {
    return this.videos.findIndex(v => v.id === this.currentVideo?.id);
  }

  getStreamUrl(id: number): string {
    return this.videoService.getStreamUrl(id);
  }

  onLoadedMetadata(): void {
    const video = this.videoElement.nativeElement;
    this.duration = video.duration;
    this.bufferEnd = this.getBufferEnd();

    if (this.pendingSeek !== null) {
      const target = this.pendingSeek;
      this.pendingSeek = null;
      try {
        video.currentTime = target;
      } catch (e) {
        this.playerError = true;
        this.isBuffering = false;
        return;
      }
      this.dubSeekTo(target);
      if (this.resyncWasPlaying) {
        video.play()
          .then(() => { this.isPlaying = true; this.isResyncing = false; })
          .catch(() => { this.isPlaying = video.paused === false; this.isResyncing = false; });
      }
      this.resyncWasPlaying = false;
    }
  }

  onTimeUpdate(): void {
    this.currentTime = this.videoElement.nativeElement.currentTime;
    const end = this.getBufferEnd();
    if (end > this.bufferEnd) {
      this.bufferEnd = end;
    }
    this.syncDubAudio();
    this.captureFrame();
  }

  onProgress(): void {
    this.bufferEnd = this.getBufferEnd();
  }

  onWaiting(): void {
    this.showBuffering();
    this.scheduleStallResync();
  }

  onStalled(): void {
    this.showBuffering();
    this.scheduleStallResync();
  }

  onPlaying(): void {
    this.isPlaying = true;
    this.hideBuffering();
    this.isResyncing = false;
    this.playerError = false;
    this.resyncAttempts = 0;
    this.clearStallTimer();
    this.dubPlay();
  }

  onCanPlay(): void {
    this.hideBuffering();
    this.clearStallTimer();
  }

  onLoadedData(): void {
    this.hideBuffering();
    this.clearStallTimer();
    this.captureFrame();
  }

  onSeeking(): void {
    this.hideBuffering();
  }

  onSeeked(): void {
    this.hideBuffering();
    this.captureFrame();
    const video = this.videoElement?.nativeElement;
    if (video) {
      this.dubSeekTo(video.currentTime);
    }
  }

  private captureFrame(): void {
    const video = this.videoElement?.nativeElement;
    if (!video || !video.videoWidth || !video.videoHeight) return;
    const now = performance.now();
    if (now - this.lastCapture < VideoPlayerComponent.CAPTURE_EVERY_MS) return;
    this.lastCapture = now;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    try {
      ctx.drawImage(video, 0, 0);
      this.posterImage = canvas.toDataURL('image/jpeg', 0.6);
    } catch (e) {}
  }

  onError(): void {
    this.isPlaying = false;
    this.showBuffering();
    this.scheduleStallResync();
  }

  private scheduleStallResync(): void {
    if (this.stallTimer || this.isResyncing) return;
    this.stallTimer = setTimeout(() => {
      this.stallTimer = null;
      if (!this.isBuffering) return;
      this.resyncAttempts++;
      this.resyncVideo();
    }, VideoPlayerComponent.STALL_RESYNC_DELAY);
  }

  resyncVideo(): void {
    const video = this.videoElement?.nativeElement;
    if (!video || Number.isNaN(video.currentTime)) return;
    this.resyncWasPlaying = !video.paused || this.isPlaying;
    this.pendingSeek = video.currentTime;
    this.isResyncing = true;
    this.showBuffering();
    this.playerError = false;
    this.clearStallTimer();
    this.dubPause();
    if (!video.paused) {
      video.pause();
    }
    this.isPlaying = false;
    video.load();
  }

  reloadVideo(): void {
    this.playerError = false;
    this.resyncAttempts = 0;
    this.resyncVideo();
  }

  private getBufferEnd(): number {
    const video = this.videoElement?.nativeElement;
    if (!video) return 0;
    const ranges = video.buffered;
    const duration = video.duration;
    if (!ranges || ranges.length === 0 || !duration || Number.isNaN(duration)) return 0;
    let end = 0;
    for (let i = 0; i < ranges.length; i++) {
      if (ranges.start(i) <= video.currentTime + 0.5) {
        end = Math.max(end, ranges.end(i));
      }
    }
    return Math.min(end, duration);
  }

  get bufferPercent(): number {
    if (!this.duration) return 0;
    return Math.min(Math.max((this.bufferEnd / this.duration) * 100, 0), 100);
  }

  get seekTrackStyle(): string {
    const duration = this.duration || 0;
    if (!duration) {
      return 'linear-gradient(to right, #dc2626 0%, rgba(255,255,255,0.15) 0%)';
    }
    const played = Math.min(Math.max((this.currentTime / duration) * 100, 0), 100);
    const buffered = Math.max(played, Math.min(Math.max((this.bufferEnd / duration) * 100, 0), 100));
    return `linear-gradient(to right, #dc2626 0%, #dc2626 ${played}%, rgba(255,255,255,0.45) ${played}%, rgba(255,255,255,0.45) ${buffered}%, rgba(255,255,255,0.15) ${buffered}%, rgba(255,255,255,0.15) 100%)`;
  }

  // Zone tampon de doublage générée en avance (tampon glissant de 20 à 30 secondes)
  get dubBufferAhead(): number {
    if (!this.dubActive || this.activeTrack === 'vo') return 0;
    const job = this.dubJobs.get(this.activeTrack);
    if (job?.status === 'READY') {
      return Math.max(0, (this.duration || 0) - this.currentTime);
    }
    if (!this.liveSegmentsList || this.liveSegmentsList.length === 0) return 0;
    const maxEnd = this.liveSegmentsList.reduce((max, s) => Math.max(max, s.end), 0);
    return Math.max(0, maxEnd - this.currentTime);
  }

  get dubBufferLeftPercent(): number {
    const duration = this.duration || 0;
    if (!duration || !this.dubActive || this.activeTrack === 'vo') return 0;
    return Math.min(Math.max((this.currentTime / duration) * 100, 0), 100);
  }

  get dubBufferWidthPercent(): number {
    const duration = this.duration || 0;
    if (!duration || !this.dubActive || this.activeTrack === 'vo') return 0;
    const ahead = this.dubBufferAhead;
    if (ahead <= 0) return 0;
    const widthPct = (ahead / duration) * 100;
    const leftPct = this.dubBufferLeftPercent;
    return Math.min(widthPct, 100 - leftPct);
  }

  // Gestion des modèles hors-ligne Argos Translate
  openArgosModal(): void {
    this.showArgosModal = true;
    this.showAudioMenu = false;
    this.fetchArgosModels();
  }

  closeArgosModal(): void {
    this.showArgosModal = false;
    if (this.argosPollTimer) {
      clearInterval(this.argosPollTimer);
      this.argosPollTimer = null;
    }
  }

  fetchArgosModels(): void {
    this.dubService.getArgosModels().subscribe({
      next: (models) => {
        this.argosModels = models;
        const hasDownloading = models.some(m => m.downloading);
        if (hasDownloading && !this.argosPollTimer) {
          this.argosPollTimer = setInterval(() => this.fetchArgosModels(), 2000);
        } else if (!hasDownloading && this.argosPollTimer) {
          clearInterval(this.argosPollTimer);
          this.argosPollTimer = null;
          this.isDownloadingArgosAll = false;
        }
      },
      error: (err) => console.warn('Erreur chargement modèles Argos:', err)
    });
  }

  downloadArgosModel(lang: string): void {
    const model = this.argosModels.find(m => m.lang === lang);
    if (model) {
      model.downloading = true;
      model.progress = 1;
    }
    this.dubService.downloadArgosModel(lang).subscribe({
      next: () => {
        if (!this.argosPollTimer) {
          this.argosPollTimer = setInterval(() => this.fetchArgosModels(), 2000);
        }
      },
      error: (err) => {
        console.error('Erreur téléchargement modèle Argos:', err);
        if (model) model.downloading = false;
      }
    });
  }

  downloadAllArgosModels(): void {
    this.isDownloadingArgosAll = true;
    this.argosModels.forEach(m => {
      if (!m.installed) {
        m.downloading = true;
        m.progress = 1;
      }
    });
    this.dubService.downloadAllArgosModels().subscribe({
      next: () => {
        if (!this.argosPollTimer) {
          this.argosPollTimer = setInterval(() => this.fetchArgosModels(), 2000);
        }
      },
      error: (err) => {
        console.error('Erreur téléchargement groupé Argos:', err);
        this.isDownloadingArgosAll = false;
      }
    });
  }

  get isAllArgosInstalled(): boolean {
    return this.argosModels.length > 0 && this.argosModels.every(m => m.installed);
  }

  togglePlay(): void {
    const video = this.videoElement.nativeElement;
    if (video.paused) {
      this.playerError = false;
      this.isPlaying = true;
      video.play()
        .then(() => {
          this.isPlaying = true;
          this.isBuffering = false;
        })
        .catch(() => {
          this.isPlaying = video.paused === false;
          this.isBuffering = false;
        });
      this.dubPlay();
    } else {
      video.pause();
      this.isPlaying = false;
      this.dubPause();
    }
  }

  toggleMute(): void {
    this.isMuted = !this.isMuted;
    const video = this.videoElement?.nativeElement;
    const dub = this.dubAudioElement?.nativeElement;
    const segAudio = this.segmentAudioElement?.nativeElement;

    if (dub) dub.muted = this.isMuted;
    if (segAudio) segAudio.muted = this.isMuted;
    if (video && (!this.dubActive || this.activeTrack === 'vo' || this.playingSegmentIndex === null)) {
      video.muted = this.isMuted;
    }
  }

  onVolumeChange(event: any): void {
    this.volume = Number(event.target.value);
    this.isMuted = false;
    const video = this.videoElement?.nativeElement;
    const dub = this.dubAudioElement?.nativeElement;
    const segAudio = this.segmentAudioElement?.nativeElement;

    if (dub) {
      dub.volume = this.volume;
      dub.muted = false;
    }
    if (segAudio) {
      segAudio.volume = this.volume;
      segAudio.muted = false;
    }
    if (video) {
      video.volume = this.volume;
      if (!this.dubActive || this.activeTrack === 'vo' || this.playingSegmentIndex === null) {
        video.muted = false;
      }
    }
  }

  seekTo(event: any): void {
    const video = this.videoElement.nativeElement;
    const newTime = Number(event.target.value);
    video.currentTime = newTime;
    this.currentTime = newTime;
    this.dubSeekTo(newTime);
  }

  seekToTime(time: number): void {
    const video = this.videoElement.nativeElement;
    video.currentTime = time;
    this.currentTime = time;
    this.dubSeekTo(time);
  }

  skip(seconds: number): void {
    const video = this.videoElement.nativeElement;
    video.currentTime += seconds;
    this.currentTime = video.currentTime;
    this.dubSeekTo(video.currentTime);
  }

  toggleFullscreen(): void {
    const container = document.getElementById('video-container');
    if (!container) return;

    if (document.fullscreenElement) {
      document.exitFullscreen();
      this.isFullscreen = false;
    } else {
      container.requestFullscreen();
      this.isFullscreen = true;
    }
  }

  onEnded(): void {
    this.isPlaying = false;

    if (this.loopMode === 'one') {
      const video = this.videoElement.nativeElement;
      video.currentTime = 0;
      const restart = () => {
        video.play()
          .then(() => { this.isPlaying = true; })
          .catch(() => { this.isPlaying = video.paused === false; });
      };
      requestAnimationFrame(() => {
        try {
          restart();
        } catch (e) {
          this.isPlaying = false;
        }
      });
      this.dubSeekTo(0);
      this.dubPlay();
      return;
    }

    if (this.loopMode === 'all' && this.videos.length > 0) {
      const nextIndex = (this.currentIndex + 1) % this.videos.length;
      this.selectVideo.emit(this.videos[nextIndex]);
      return;
    }

    this.videoEnded.emit();
  }

  cycleLoopMode(): void {
    const idx = this.LOOP_MODES.indexOf(this.loopMode);
    this.loopMode = this.LOOP_MODES[(idx + 1) % this.LOOP_MODES.length];
    this.loopModeChange.emit(this.loopMode);
  }

  nextVideo(): void {
    if (this.currentIndex < this.videos.length - 1) {
      this.selectVideo.emit(this.videos[this.currentIndex + 1]);
    } else if (this.loopMode === 'all' && this.videos.length > 0) {
      this.selectVideo.emit(this.videos[0]);
    }
  }

  previousVideo(): void {
    if (this.currentIndex > 0) {
      this.selectVideo.emit(this.videos[this.currentIndex - 1]);
    } else if (this.loopMode === 'all' && this.videos.length > 0) {
      this.selectVideo.emit(this.videos[this.videos.length - 1]);
    }
  }

  onSelectVideo(video: Video): void {
    this.selectVideo.emit(video);
  }

  showControlsTemporarily(): void {
    this.showControls = true;
    clearTimeout(this.controlTimeout);
    this.controlTimeout = setTimeout(() => {
      if (this.isPlaying) {
        this.showControls = false;
      }
    }, 3000);
  }

  onMouseMove(): void {
    this.showControlsTemporarily();
  }

  changeRate(rate: number): void {
    const video = this.videoElement.nativeElement;
    video.playbackRate = rate;
    const dub = this.dubAudioElement?.nativeElement;
    if (dub) {
      dub.playbackRate = rate;
    }
    const segAudio = this.segmentAudioElement?.nativeElement;
    if (segAudio) {
      segAudio.playbackRate = rate;
    }
    this.playbackRate = rate;
    this.showSpeedMenu = false;
  }

  formatTime(seconds: number): string {
    if (!seconds || isNaN(seconds)) return '0:00';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`;
  }
}
