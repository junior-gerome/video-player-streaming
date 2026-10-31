import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoService } from '../../services/video.service';
import { Video } from '../../models/video.model';

@Component({
  selector: 'app-upload-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload-dialog.component.html',
  styleUrl: './upload-dialog.component.css'
})
export class UploadDialogComponent {

  @Output() uploadComplete = new EventEmitter<Video>();
  @Output() closed = new EventEmitter<void>();

  selectedFile: File | null = null;
  title: string = '';
  description: string = '';
  uploading = false;
  uploadProgress = 0;
  error: string = '';
  dragOver = false;

  constructor(private videoService: VideoService) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.validateAndSetFile(file);
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files[0];
    if (file) {
      this.validateAndSetFile(file);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = true;
  }

  onDragLeave(): void {
    this.dragOver = false;
  }

  validateAndSetFile(file: File): void {
    if (file.size > 2 * 1024 * 1024 * 1024) {
      this.error = 'Le fichier est trop volumineux (max 2GB)';
      return;
    }
    this.error = '';
    this.selectedFile = file;
    const nameWithoutExt = file.name.replace(/\.[^.]+$/, '');
    if (!this.title) {
      this.title = nameWithoutExt;
    }
  }

  removeFile(): void {
    this.selectedFile = null;
    this.title = '';
    this.description = '';
    this.uploadProgress = 0;
  }

  upload(): void {
    if (!this.selectedFile || !this.title.trim()) {
      this.error = 'Veuillez sélectionner un fichier et saisir un titre';
      return;
    }

    this.uploading = true;
    this.uploadProgress = 0;
    this.error = '';

    const interval = setInterval(() => {
      if (this.uploadProgress < 90) {
        this.uploadProgress += 5;
      }
    }, 200);

    this.videoService.uploadVideo(this.selectedFile, this.title.trim(), this.description.trim())
      .subscribe({
        next: (video) => {
          clearInterval(interval);
          this.uploadProgress = 100;
          setTimeout(() => {
            this.uploading = false;
            this.uploadComplete.emit(video);
          }, 500);
        },
        error: (err) => {
          clearInterval(interval);
          this.uploading = false;
          this.error = err.error?.message || 'Erreur lors de l\'upload';
        }
      });
  }

  close(): void {
    this.closed.emit();
  }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`;
  }
}
