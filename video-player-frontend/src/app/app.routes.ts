import { Routes } from '@angular/router';
import { VideoListComponent } from './components/video-list/video-list.component';
import { PlaylistManagerComponent } from './components/playlist-manager/playlist-manager.component';

export const routes: Routes = [
  { path: '', redirectTo: '/videos', pathMatch: 'full' },
  { path: 'videos', component: VideoListComponent },
  { path: 'playlists', component: PlaylistManagerComponent },
  { path: '**', redirectTo: '/videos' }
];
