import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Video } from '../models/video.model';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class VideoService {

  private apiUrl = 'http://localhost:8080/api/videos';

  constructor(private http: HttpClient) {}

  getAllVideos(page: number = 0, size: number = 12): Observable<PageResponse<Video>> {
    return this.http.get<PageResponse<Video>>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getVideoById(id: number): Observable<Video> {
    return this.http.get<Video>(`${this.apiUrl}/${id}`);
  }

  searchVideos(query: string, page: number = 0, size: number = 12): Observable<PageResponse<Video>> {
    return this.http.get<PageResponse<Video>>(`${this.apiUrl}/search?q=${query}&page=${page}&size=${size}`);
  }

  uploadVideo(file: File, title: string, description: string): Observable<Video> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    formData.append('description', description);
    return this.http.post<Video>(`${this.apiUrl}/upload`, formData);
  }

  deleteVideo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getStreamUrl(id: number): string {
    return `${this.apiUrl}/${id}/stream`;
  }
}
