import { Video } from './video.model';

export type LoopMode = 'none' | 'one' | 'all';

export interface Playlist {
  id: number;
  name: string;
  description: string;
  loopMode: LoopMode;
  createdAt: string;
  videos: Video[];
  videoCount: number;
}
