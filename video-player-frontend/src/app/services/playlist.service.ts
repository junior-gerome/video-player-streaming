import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Playlist, LoopMode } from '../models/playlist.model';

@Injectable({
  providedIn: 'root'
})
export class PlaylistService {

  private apiUrl = 'http://localhost:8080/api/playlists';

  constructor(private http: HttpClient) {}

  getAllPlaylists(): Observable<Playlist[]> {
    return this.http.get<Playlist[]>(this.apiUrl);
  }

  getPlaylistById(id: number): Observable<Playlist> {
    return this.http.get<Playlist>(`${this.apiUrl}/${id}`);
  }

  createPlaylist(name: string, description: string, loopMode: LoopMode = 'none'): Observable<Playlist> {
    return this.http.post<Playlist>(this.apiUrl, { name, description, loopMode });
  }

  setLoopMode(id: number, loopMode: LoopMode): Observable<Playlist> {
    return this.http.put<Playlist>(`${this.apiUrl}/${id}/loop-mode`, { loopMode });
  }

  updatePlaylist(id: number, name: string, description: string): Observable<Playlist> {
    return this.http.put<Playlist>(`${this.apiUrl}/${id}`, { name, description });
  }

  addVideosToPlaylist(id: number, videoIds: number[]): Observable<Playlist> {
    return this.http.put<Playlist>(`${this.apiUrl}/${id}/videos`, { videoIds });
  }

  removeVideoFromPlaylist(playlistId: number, videoId: number): Observable<Playlist> {
    return this.http.delete<Playlist>(`${this.apiUrl}/${playlistId}/videos/${videoId}`);
  }

  deletePlaylist(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
