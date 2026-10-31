export type DubJobStatus = 'PENDING' | 'PROCESSING' | 'STREAMING' | 'READY' | 'ERROR';

export interface DubJob {
  id: number;
  videoId: number;
  videoTitle: string;
  targetLang: string;
  status: DubJobStatus;
  progress: number;
  message: string;
  error: string | null;
  audioUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DubSegment {
  index: number;
  start: number;
  end: number;
  audioUrl: string;
}