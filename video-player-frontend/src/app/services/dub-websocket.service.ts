import { Injectable, OnDestroy } from '@angular/core';
import { Subject, Observable, BehaviorSubject } from 'rxjs';
import { DubJob, DubSegment } from '../models/dub-job.model';

export interface WebSocketDubMessage {
  type: 'JOB_STATUS' | 'SEGMENT' | 'SEGMENTS_BATCH' | 'PROGRESS' | 'AUDIO_READY' | 'PONG';
  data?: any;
  progress?: number;
  message?: string;
  audioUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DubWebSocketService implements OnDestroy {

  private wsUrl = 'ws://localhost:8080/ws/dub';
  private socket: WebSocket | null = null;
  private reconnectTimer: any = null;
  private isDestroyed = false;

  private currentSubscription: { videoId: number; targetLang: string } | null = null;

  private retryDelay = 2000;
  private readonly maxRetryDelay = 20000;
  private hasWarnedDisconnect = false;

  private connectedSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.connectedSubject.asObservable();

  public get isSocketOpen(): boolean {
    return this.socket?.readyState === WebSocket.OPEN;
  }

  private jobSubject = new Subject<DubJob>();
  private segmentSubject = new Subject<DubSegment>();
  private segmentsBatchSubject = new Subject<DubSegment[]>();
  private progressSubject = new Subject<{ progress: number; message: string }>();
  private audioReadySubject = new Subject<string>();

  public job$ = this.jobSubject.asObservable();
  public segment$ = this.segmentSubject.asObservable();
  public segmentsBatch$ = this.segmentsBatchSubject.asObservable();
  public progress$ = this.progressSubject.asObservable();
  public audioReady$ = this.audioReadySubject.asObservable();

  constructor() {
    this.connect();
  }

  private connect(): void {
    if (this.isDestroyed) return;

    try {
      this.socket = new WebSocket(this.wsUrl);

      this.socket.onopen = () => {
        this.connectedSubject.next(true);
        this.retryDelay = 2000;
        this.hasWarnedDisconnect = false;

        // Réabonnement automatique après reconnexion
        if (this.currentSubscription) {
          this.subscribe(this.currentSubscription.videoId, this.currentSubscription.targetLang);
        }
      };

      this.socket.onmessage = (event: MessageEvent) => {
        try {
          const msg: WebSocketDubMessage = JSON.parse(event.data);
          this.handleIncomingMessage(msg);
        } catch (err) {
          console.warn('Erreur lecture message WebSocket:', err);
        }
      };

      this.socket.onclose = () => {
        this.connectedSubject.next(false);
        this.scheduleReconnect();
      };

      this.socket.onerror = () => {
        this.connectedSubject.next(false);
        this.socket?.close();
      };
    } catch (e) {
      this.connectedSubject.next(false);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect(): void {
    if (this.isDestroyed || this.reconnectTimer) return;

    if (!this.hasWarnedDisconnect) {
      console.info(`[DubWebSocket] Connexion à ${this.wsUrl} indisponible ou en attente (retry dans ${this.retryDelay / 1000}s). Note: Assurez-vous d'avoir redémarré le backend Spring Boot pour charger l'endpoint /ws/dub.`);
      this.hasWarnedDisconnect = true;
    }

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.retryDelay = Math.min(this.retryDelay * 1.5, this.maxRetryDelay);
      this.connect();
    }, this.retryDelay);
  }

  private handleIncomingMessage(msg: WebSocketDubMessage): void {
    switch (msg.type) {
      case 'JOB_STATUS':
        if (msg.data) this.jobSubject.next(msg.data);
        break;
      case 'SEGMENT':
        if (msg.data) this.segmentSubject.next(msg.data);
        break;
      case 'SEGMENTS_BATCH':
        if (msg.data && Array.isArray(msg.data)) this.segmentsBatchSubject.next(msg.data);
        break;
      case 'PROGRESS':
        if (msg.progress !== undefined) {
          this.progressSubject.next({ progress: msg.progress, message: msg.message || '' });
        }
        break;
      case 'AUDIO_READY':
        if (msg.audioUrl) this.audioReadySubject.next(msg.audioUrl);
        break;
    }
  }

  public subscribe(videoId: number, targetLang: string = 'fr'): void {
    this.currentSubscription = { videoId, targetLang };
    this.send({
      action: 'SUBSCRIBE',
      videoId,
      targetLang
    });
  }

  public startDub(
    videoId: number,
    targetLang: string = 'fr',
    options?: { force?: boolean; cloneVoice?: boolean; demucs?: boolean }
  ): void {
    this.currentSubscription = { videoId, targetLang };
    this.send({
      action: 'START_DUB',
      videoId,
      targetLang,
      force: options?.force ?? false,
      cloneVoice: options?.cloneVoice ?? false,
      demucs: options?.demucs ?? true
    });
  }

  public seek(videoId: number, targetLang: string, position: number): void {
    this.send({
      action: 'SEEK',
      videoId,
      targetLang,
      position
    });
  }

  private send(payload: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(payload));
    } else {
      // Si la socket n'est pas encore prête, on attend l'ouverture
      const checkAndSend = () => {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
          this.socket.send(JSON.stringify(payload));
        }
      };
      setTimeout(checkAndSend, 300);
    }
  }

  ngOnDestroy(): void {
    this.isDestroyed = true;
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
}
