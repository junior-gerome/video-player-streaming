export interface Video {
  id: number;
  title: string;
  description: string;
  filename: string;
  contentType: string;
  fileSize: number;
  duration: number | null;
  createdAt: string;
  streamUrl: string;
}
