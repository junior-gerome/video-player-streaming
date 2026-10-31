import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';

export interface JobView { id: string; status: string; error: string | null; }

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080/api/translations';

  create(file: File, languages: string[]): Observable<JobView> {
    const form = new FormData();
    form.append('file', file);
    languages.forEach(l => form.append('languages', l));
    return this.http.post<JobView>(this.api, form);
  }

  poll(id: string): Observable<JobView> {
    return timer(0, 2000).pipe(
      switchMap(() => this.http.get<JobView>(`${this.api}/${id}`)),
      takeWhile(j => j.status !== 'DONE' && j.status !== 'FAILED', true)
    );
  }

  downloadUrl(id: string) { return `${this.api}/${id}/download`; }
}
