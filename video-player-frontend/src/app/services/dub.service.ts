import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DubJob, DubSegment } from '../models/dub-job.model';

export interface SupportedLanguage {
  code: string;
  label: string;
  nativeName: string;
  flag: string;
}

export const SUPPORTED_LANGUAGES: SupportedLanguage[] = [
  { code: 'fr', label: 'Français', nativeName: 'Français', flag: '🇫🇷' },
  { code: 'de', label: 'Allemand', nativeName: 'Deutsch', flag: '🇩🇪' },
  { code: 'es', label: 'Espagnol', nativeName: 'Español', flag: '🇪🇸' },
  { code: 'it', label: 'Italien', nativeName: 'Italiano', flag: '🇮🇹' }
];

export interface ArgosModel {
  lang: string;
  label: string;
  fromLang: string;
  fileName: string;
  downloadUrl: string;
  installed: boolean;
  sizeMb: number;
  downloading: boolean;
  progress: number;
}

export interface DubSseEvent {
  type: 'job' | 'segment' | 'segments';
  data: any;
}

@Injectable({
  providedIn: 'root'
})
export class DubService {

  private apiUrl = 'http://localhost:8080/api/dub';

  constructor(private http: HttpClient) {}

  getArgosModels(): Observable<ArgosModel[]> {
    return this.http.get<ArgosModel[]>(`${this.apiUrl}/models/argos`);
  }

  downloadArgosModel(lang: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/models/argos/download/${lang}`, {});
  }

  downloadAllArgosModels(): Observable<any> {
    return this.http.post(`${this.apiUrl}/models/argos/download-all`, {});
  }

  generate(videoId: number, targetLang: string = 'fr', force: boolean = false, cloneVoice: boolean = false, demucs: boolean = true): Observable<DubJob> {
    return this.http.post<DubJob>(`${this.apiUrl}/${videoId}/generate`, { targetLang, force, cloneVoice, demucs });
  }

  prioritizeSeek(videoId: number, targetLang: string = 'fr', position: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${videoId}/seek`, { targetLang, position });
  }

  subscribeEvents(videoId: number, targetLang: string = 'fr'): Observable<DubSseEvent> {
    return new Observable<DubSseEvent>(observer => {
      const url = `${this.apiUrl}/${videoId}/events?lang=${targetLang}`;
      const eventSource = new EventSource(url);

      eventSource.addEventListener('job', (e: MessageEvent) => {
        try {
          const job: DubJob = JSON.parse(e.data);
          observer.next({ type: 'job', data: job });
        } catch (err) {
          console.warn('Erreur parsing SSE job:', err);
        }
      });

      eventSource.addEventListener('segment', (e: MessageEvent) => {
        try {
          const seg: DubSegment = JSON.parse(e.data);
          observer.next({ type: 'segment', data: seg });
        } catch (err) {
          console.warn('Erreur parsing SSE segment:', err);
        }
      });

      eventSource.addEventListener('segments', (e: MessageEvent) => {
        try {
          const segs: DubSegment[] = JSON.parse(e.data);
          observer.next({ type: 'segments', data: segs });
        } catch (err) {
          console.warn('Erreur parsing SSE segments:', err);
        }
      });

      eventSource.onerror = (error) => {
        // SSE peut se reconnecter automatiquement, ou fermer si le serveur termine
        if (eventSource.readyState === EventSource.CLOSED) {
          observer.complete();
        }
      };

      return () => {
        eventSource.close();
      };
    });
  }

  getJobForVideo(videoId: number, lang?: string): Observable<DubJob> {
    const url = lang ? `${this.apiUrl}/video/${videoId}?lang=${lang}` : `${this.apiUrl}/video/${videoId}`;
    return this.http.get<DubJob>(url);
  }

  getAllTracksForVideo(videoId: number): Observable<DubJob[]> {
    return this.http.get<DubJob[]>(`${this.apiUrl}/video/${videoId}/tracks`);
  }

  getSegments(videoId: number, lang: string = 'fr'): Observable<DubSegment[]> {
    return this.http.get<DubSegment[]>(`${this.apiUrl}/${videoId}/segments?lang=${lang}`);
  }

  getJob(jobId: number): Observable<DubJob> {
    return this.http.get<DubJob>(`${this.apiUrl}/${jobId}`);
  }

  deleteForVideo(videoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/video/${videoId}`);
  }

  getAudioUrl(videoId: number, lang: string = 'fr'): string {
    return `${this.apiUrl}/${videoId}/audio?lang=${lang}`;
  }
}