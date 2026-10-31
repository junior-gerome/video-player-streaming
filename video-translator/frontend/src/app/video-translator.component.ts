import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { JobView, TranslationService } from './translation.service';

@Component({
  selector: 'app-video-translator',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>Traduction vidéo multilingue (100 % open source)</h2>

    <input type="file" accept="video/*" (change)="onFile($event)" />

    <fieldset>
      <legend>Langues des pistes audio</legend>
      @for (l of available; track l.code) {
        <label>
          <input type="checkbox" [checked]="selected().includes(l.code)" (change)="toggle(l.code)" />
          {{ l.label }}
        </label>
      }
    </fieldset>

    <button [disabled]="!file() || !selected().length || busy()" (click)="start()">Traduire</button>

    @if (job(); as j) {
      <p>Statut : <strong>{{ j.status }}</strong></p>
      @if (j.status === 'DONE') { <a [href]="svc.downloadUrl(j.id)">Télécharger la vidéo</a> }
      @if (j.error) { <p class="error">{{ j.error }}</p> }
    }
  `,
})
export class VideoTranslatorComponent {
  svc = inject(TranslationService);

  // codes = voix disponibles dans le service IA (ai-service/main.py -> VOICES)
  available = [
    { code: 'en', label: 'Anglais' }, { code: 'fr', label: 'Français' },
    { code: 'es', label: 'Espagnol' }, { code: 'de', label: 'Allemand' },
    { code: 'ar', label: 'Arabe' }, { code: 'pt', label: 'Portugais' },
  ];
  file = signal<File | null>(null);
  selected = signal<string[]>([]);
  job = signal<JobView | null>(null);
  busy = signal(false);

  onFile(e: Event) { this.file.set((e.target as HTMLInputElement).files?.[0] ?? null); }

  toggle(code: string) {
    this.selected.update(s => (s.includes(code) ? s.filter(x => x !== code) : [...s, code]));
  }

  start() {
    this.busy.set(true);
    this.svc.create(this.file()!, this.selected())
      .pipe(switchMap(j => this.svc.poll(j.id)))
      .subscribe({
        next: j => { this.job.set(j); if (j.status === 'DONE' || j.status === 'FAILED') this.busy.set(false); },
        error: () => this.busy.set(false),
      });
  }
}
