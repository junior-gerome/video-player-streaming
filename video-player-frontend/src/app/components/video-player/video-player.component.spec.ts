import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { of, Subject } from 'rxjs';
import { VideoPlayerComponent } from './video-player.component';
import { DubService } from '../../services/dub.service';
import { DubWebSocketService } from '../../services/dub-websocket.service';
import { Video } from '../../models/video.model';
import { DubJob, DubSegment } from '../../models/dub-job.model';

describe('VideoPlayerComponent', () => {
  let component: VideoPlayerComponent;
  let fixture: ComponentFixture<VideoPlayerComponent>;
  let dubServiceSpy: jasmine.SpyObj<DubService>;
  let dubWsSpy: any;

  const mockVideo: Video = {
    id: 1,
    title: 'Test Video',
    description: 'Test description',
    fileName: 'test.mp4',
    contentType: 'video/mp4',
    size: 10240,
    duration: 120,
    objectName: 'videos/test.mp4',
    createdAt: new Date().toISOString()
  };

  beforeEach(async () => {
    dubServiceSpy = jasmine.createSpyObj('DubService', [
      'getAllTracksForVideo',
      'getSegments',
      'generate',
      'prioritizeSeek',
      'subscribeEvents',
      'getJobForVideo',
      'getAudioUrl',
      'getArgosModels',
      'downloadArgosModel',
      'downloadAllArgosModels'
    ]);

    dubServiceSpy.getAllTracksForVideo.and.returnValue(of([]));
    dubServiceSpy.getSegments.and.returnValue(of([]));
    dubServiceSpy.subscribeEvents.and.returnValue(of());
    dubServiceSpy.getAudioUrl.and.returnValue('/api/dub/1/audio?lang=fr');
    dubServiceSpy.getArgosModels.and.returnValue(of([]));
    dubServiceSpy.downloadArgosModel.and.returnValue(of({}));
    dubServiceSpy.downloadAllArgosModels.and.returnValue(of({}));

    dubWsSpy = {
      job$: new Subject<DubJob>(),
      segment$: new Subject<DubSegment>(),
      segmentsBatch$: new Subject<DubSegment[]>(),
      progress$: new Subject<{ progress: number; message: string }>(),
      audioReady$: new Subject<string>(),
      subscribe: jasmine.createSpy('subscribe'),
      startDub: jasmine.createSpy('startDub'),
      seek: jasmine.createSpy('seek')
    };

    await TestBed.configureTestingModule({
      imports: [VideoPlayerComponent],
      providers: [
        provideHttpClient(),
        { provide: DubService, useValue: dubServiceSpy },
        { provide: DubWebSocketService, useValue: dubWsSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VideoPlayerComponent);
    component = fixture.componentInstance;
    component.currentVideo = mockVideo;
    fixture.detectChanges();
  });

  it('should create the video player component', () => {
    expect(component).toBeTruthy();
  });

  it('should start instant playback and send START_DUB via WebSocket on language click', () => {
    const videoEl = component.videoElement.nativeElement;
    spyOn(videoEl, 'play').and.returnValue(Promise.resolve());

    component.selectAudioTrack('fr');

    expect(component.activeTrack).toBe('fr');
    expect(component.dubActive).toBeTrue();
    expect(videoEl.play).toHaveBeenCalled();
    expect(dubWsSpy.startDub).toHaveBeenCalledWith(1, 'fr', {
      cloneVoice: false,
      demucs: true,
      force: false
    });
  });

  it('should restore VO audio when no segment is playing at current time', () => {
    component.activeTrack = 'fr';
    component.dubActive = true;
    component.liveSegmentsList = [
      { index: 0, start: 10.0, end: 15.0, audioUrl: '/api/dub/1/segments/0?lang=fr' }
    ];
    component.currentTime = 2.0;

    const videoEl = component.videoElement.nativeElement;
    (component as any).syncDubAudio();

    expect(videoEl.muted).toBeFalse();
    expect(component.playingSegmentIndex).toBeNull();
  });

  it('should send SEEK command via bidirectional WebSocket when seeking', () => {
    component.activeTrack = 'fr';
    component.dubActive = true;

    component.seekToTime(45.0);

    expect(dubWsSpy.seek).toHaveBeenCalledWith(1, 'fr', 45.0);
  });

  it('should toggle Demucs background music and voice cloning options', () => {
    expect(component.demucsEnabled).toBeTrue();
    expect(component.cloneVoiceEnabled).toBeFalse();

    component.demucsEnabled = false;
    component.cloneVoiceEnabled = true;

    const videoEl = component.videoElement.nativeElement;
    spyOn(videoEl, 'play').and.returnValue(Promise.resolve());

    component.selectAudioTrack('de');

    expect(dubWsSpy.startDub).toHaveBeenCalledWith(1, 'de', {
      cloneVoice: true,
      demucs: false,
      force: false
    });
  });

  it('should compute the sliding dubbing buffer ahead on the timeline', () => {
    component.duration = 100;
    component.currentTime = 10;
    component.dubActive = true;
    component.activeTrack = 'fr';
    component.liveSegmentsList = [
      { index: 0, start: 0, end: 15, audioUrl: '/seg0' },
      { index: 1, start: 15, end: 35, audioUrl: '/seg1' } // Buffer reaches 35s (ahead = 35 - 10 = 25s)
    ];

    expect(component.dubBufferAhead).toBe(25);
    expect(component.dubBufferLeftPercent).toBe(10);
    expect(component.dubBufferWidthPercent).toBe(25);
  });

  it('should open Argos offline model manager and trigger 1-click download', () => {
    component.openArgosModal();

    expect(component.showArgosModal).toBeTrue();
    expect(dubServiceSpy.getArgosModels).toHaveBeenCalled();

    component.downloadAllArgosModels();

    expect(dubServiceSpy.downloadAllArgosModels).toHaveBeenCalled();
    expect(component.isDownloadingArgosAll).toBeTrue();
  });
});
