package com.videoplayer.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoplayer.dto.DubJobDTO;
import com.videoplayer.dto.DubSegmentDTO;
import com.videoplayer.service.DubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DubWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DubWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mapping key (videoId + "_" + lang) -> List of sessions
    private final Map<String, List<WebSocketSession>> subscribers = new ConcurrentHashMap<>();
    // Mapping session ID -> current subscribed key
    private final Map<String, String> sessionSubscriptions = new ConcurrentHashMap<>();

    private final DubService dubService;

    public DubWebSocketHandler(@Lazy DubService dubService) {
        this.dubService = dubService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Client WebSocket connecté : id={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            String action = (String) map.get("action");
            if (action == null) return;

            switch (action.toUpperCase()) {
                case "SUBSCRIBE" -> handleSubscribe(session, map);
                case "START_DUB" -> handleStartDub(session, map);
                case "SEEK" -> handleSeek(map);
                case "PING" -> sendMessage(session, Map.of("type", "PONG"));
                default -> log.warn("Action WebSocket inconnue : {}", action);
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message WebSocket: {}", e.getMessage());
        }
    }

    private void handleSubscribe(WebSocketSession session, Map<String, Object> map) throws IOException {
        Long videoId = parseLong(map.get("videoId"));
        String lang = (String) map.getOrDefault("targetLang", "fr");
        if (videoId == null) return;

        String key = buildKey(videoId, lang);
        sessionSubscriptions.put(session.getId(), key);
        subscribers.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(session);

        // Envoyer immédiatement l'état actuel et les segments en cache
        DubJobDTO job = dubService.getJobForVideo(videoId, lang);
        if (job != null) {
            sendMessage(session, Map.of("type", "JOB_STATUS", "data", job));
        }

        List<DubSegmentDTO> existing = dubService.getSegments(videoId, lang);
        if (existing != null && !existing.isEmpty()) {
            sendMessage(session, Map.of("type", "SEGMENTS_BATCH", "data", existing));
        }
    }

    private void handleStartDub(WebSocketSession session, Map<String, Object> map) throws IOException {
        Long videoId = parseLong(map.get("videoId"));
        String lang = (String) map.getOrDefault("targetLang", "fr");
        boolean force = Boolean.TRUE.equals(map.get("force"));
        boolean cloneVoice = Boolean.TRUE.equals(map.get("cloneVoice"));
        boolean demucs = Boolean.TRUE.equals(map.get("demucs"));

        if (videoId == null) return;

        // Auto-abonnement
        handleSubscribe(session, map);

        DubJobDTO job = dubService.createDubJobWithOptions(videoId, lang, force, cloneVoice, demucs);
        sendMessage(session, Map.of("type", "JOB_STATUS", "data", job));
    }

    private void handleSeek(Map<String, Object> map) {
        Long videoId = parseLong(map.get("videoId"));
        String lang = (String) map.getOrDefault("targetLang", "fr");
        double position = 0.0;
        if (map.get("position") != null) {
            try {
                position = Double.parseDouble(map.get("position").toString());
            } catch (Exception ignored) {}
        }
        if (videoId != null) {
            dubService.prioritizeSeek(videoId, lang, position);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String key = sessionSubscriptions.remove(session.getId());
        if (key != null) {
            List<WebSocketSession> list = subscribers.get(key);
            if (list != null) {
                list.remove(session);
            }
        }
        log.info("Client WebSocket déconnecté : id={}", session.getId());
    }

    public void broadcastSegment(Long videoId, String lang, DubSegmentDTO segment) {
        broadcast(buildKey(videoId, lang), Map.of("type", "SEGMENT", "data", segment));
    }

    public void broadcastJob(DubJobDTO job) {
        broadcast(buildKey(job.getVideoId(), job.getTargetLang()), Map.of("type", "JOB_STATUS", "data", job));
    }

    public void broadcastProgress(Long videoId, String lang, int progress, String message) {
        broadcast(buildKey(videoId, lang), Map.of("type", "PROGRESS", "progress", progress, "message", message));
    }

    public void broadcastAudioReady(Long videoId, String lang, String audioUrl) {
        broadcast(buildKey(videoId, lang), Map.of("type", "AUDIO_READY", "audioUrl", audioUrl));
    }

    private void broadcast(String key, Object payload) {
        List<WebSocketSession> list = subscribers.get(key);
        if (list == null || list.isEmpty()) return;

        for (WebSocketSession s : list) {
            if (s.isOpen()) {
                try {
                    sendMessage(s, payload);
                } catch (Exception e) {
                    list.remove(s);
                }
            } else {
                list.remove(s);
            }
        }
    }

    private void sendMessage(WebSocketSession session, Object payload) throws IOException {
        String json = objectMapper.writeValueAsString(payload);
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    private String buildKey(Long videoId, String lang) {
        return videoId + "_" + (lang == null ? "fr" : lang.trim().toLowerCase());
    }

    private Long parseLong(Object obj) {
        if (obj == null) return null;
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
