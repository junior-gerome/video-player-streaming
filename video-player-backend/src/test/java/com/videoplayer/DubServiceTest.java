package com.videoplayer;

import com.videoplayer.dto.DubJobDTO;
import com.videoplayer.dto.DubSegmentDTO;
import com.videoplayer.model.DubJob;
import com.videoplayer.model.DubSegment;
import com.videoplayer.model.Video;
import com.videoplayer.repository.DubJobRepository;
import com.videoplayer.repository.DubSegmentRepository;
import com.videoplayer.service.DubService;
import com.videoplayer.service.MinioService;
import com.videoplayer.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DubServiceTest {

    @Mock
    private DubJobRepository dubJobRepository;

    @Mock
    private DubSegmentRepository dubSegmentRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private VideoService videoService;

    @Mock
    private com.videoplayer.websocket.DubWebSocketHandler webSocketHandler;

    @InjectMocks
    private DubService dubService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dubService, "python", "python");
        ReflectionTestUtils.setField(dubService, "script", "dub_pipeline.py");
        ReflectionTestUtils.setField(dubService, "model", "base");
        ReflectionTestUtils.setField(dubService, "voice", "fr-FR-HenriNeural");
        ReflectionTestUtils.setField(dubService, "tempDir", "./dub-tmp");
    }

    @Test
    void testGetSegmentsFromDatabaseCache() {
        DubSegment seg1 = new DubSegment(10L, "fr", "fr-FR-HenriNeural", 0, 0.0, 3.5, "dub/10/fr/seg_0.mp3", "/api/dub/10/segments/0?lang=fr");
        DubSegment seg2 = new DubSegment(10L, "fr", "fr-FR-HenriNeural", 1, 3.8, 6.2, "dub/10/fr/seg_1.mp3", "/api/dub/10/segments/1?lang=fr");

        when(dubSegmentRepository.findByVideoIdAndTargetLangOrderBySegmentIndexAsc(10L, "fr"))
                .thenReturn(List.of(seg1, seg2));

        List<DubSegmentDTO> segments = dubService.getSegments(10L, "fr");

        assertNotNull(segments);
        assertEquals(2, segments.size());
        assertEquals(0, segments.get(0).getIndex());
        assertEquals(0.0, segments.get(0).getStart());
        assertEquals(3.5, segments.get(0).getEnd());
        assertEquals(1, segments.get(1).getIndex());
    }

    @Test
    void testReturnCachedReadyJobWithoutRegeneration() {
        DubJob readyJob = new DubJob();
        readyJob.setId(99L);
        readyJob.setVideoId(10L);
        readyJob.setTargetLang("fr");
        readyJob.setStatus(DubService.STATUS_READY);
        readyJob.setAudioObjectName("dub/10_fr.mp3");

        when(dubJobRepository.findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(10L, "fr"))
                .thenReturn(Optional.of(readyJob));

        DubJobDTO result = dubService.createDubJob(10L, "fr", false);

        assertNotNull(result);
        assertEquals(DubService.STATUS_READY, result.getStatus());
        assertEquals(99L, result.getId());
        verify(dubJobRepository, never()).delete(any());
    }

    @Test
    void testForceRegenerationClearsCache() {
        DubJob oldJob = new DubJob();
        oldJob.setId(50L);
        oldJob.setVideoId(12L);
        oldJob.setTargetLang("es");
        oldJob.setStatus(DubService.STATUS_READY);

        when(dubJobRepository.findFirstByVideoIdAndTargetLangOrderByCreatedAtDesc(12L, "es"))
                .thenReturn(Optional.of(oldJob));
        when(dubJobRepository.findAllByVideoIdAndTargetLangOrderByCreatedAtDesc(12L, "es"))
                .thenReturn(List.of(oldJob));

        Video video = new Video();
        video.setId(12L);
        video.setTitle("Sample Video");
        when(videoService.getVideoEntity(12L)).thenReturn(video);

        when(dubJobRepository.save(any(DubJob.class))).thenAnswer(invocation -> {
            DubJob j = invocation.getArgument(0);
            j.setId(51L);
            return j;
        });

        DubJobDTO result = dubService.createDubJob(12L, "es", true);

        assertNotNull(result);
        assertEquals(51L, result.getId());
        verify(dubSegmentRepository).deleteByVideoIdAndTargetLang(12L, "es");
        verify(dubJobRepository).deleteAll(List.of(oldJob));
    }
}
