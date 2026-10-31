# Diagnostic Technique du Projet Existant : Doublage en Streaming & Hors Connexion

Ce rapport identifie précisément la trajectoire d'exécution dans le projet entre le clic de l'utilisateur sur une piste de langue et l'émission du son, ainsi que les points de blocage qui empêchaient le streaming immédiat et le fonctionnement hors ligne.

---

## 1. Tracé du chemin d'exécution : Du clic à l'écoute

```
[Utilisateur clique sur 'Espagnol' dans le menu des pistes]
                   │
                   ▼ (1)
[video-player.component.ts : selectAudioTrack(track)]
   ├── Si piste 'READY' (100% terminée antérieurement) :
   │      └── Affecte dubAudioElement.src = track.audioUrl (piste globale .mp3)
   │          Synchronise currentTime et joue l'audio.
   │
   └── Si piste en cours ou non générée :
          ├── Déclenche this.generateDubForLang(track.lang) (2)
          └── Établit la connexion WebSocket dubWs.subscribe(videoId, track.lang) (3)
                   │
                   ▼ (4)
[dub.service.ts : generate()] ──HTTP POST──► [DubController.java : generate()]
                                                      │
                                                      ▼ (5)
                                             [DubService.java : createDubJobWithOptions()]
                                                      │
                                                      ▼ (6)
                                             Lancement processus OS :
                                             python -u ./scripts/dub_pipeline.py
                                                      │
                                                      ▼ (7)
                                             [dub_pipeline.py]
                                                ├── Extraction audio source (FFmpeg)
                                                ├── ASR Faster-Whisper
                                                ├── Traduction
                                                ├── Synthèse vocale TTS
                                                └── Émission standard out :
                                                    "SEGMENT_CHUNK {idx} {start} {end} {path}"
                                                      │
                   ┌──────────────────────────────────┘
                   ▼ (8)
[DubService.java : boucle de lecture proc.getInputStream()]
   ├── Réception de SEGMENT_CHUNK
   ├── Upload du segment dans MinIO ("dub/{videoId}/{lang}/seg_{idx}.mp3")
   ├── Enregistrement en base de données (DubSegmentRepository)
   └── Diffusion temps réel (DubWebSocketHandler.broadcastSegment) (9)
                   │
                   ▼ (10)
[video-player.component.ts : dubWs.onSegment()]
   ├── Ajout du segment dans liveSegmentsList[]
   └── Déclenchement de syncDubAudio()
          ├── Recherche du segment correspondant à video.currentTime
          ├── Affectation segmentAudioElement.src = "/api/dub/{videoId}/segments/{idx}?lang={lang}"
          └── Lecture synchronisée et coupure du son VO
```

---

## 2. Analyse des points de blocage : Pourquoi le streaming échouait

### Blocage A : Deadlock C-Runtime Stdin/Stdout sous Windows
- **Localisation** : `video-player-backend/scripts/dub_pipeline.py` (anciennement lignes 276-295).
- **Mécanisme** : Le thread d'écoute du seek utilisait `sys.stdin.readline()`. Sous Windows, l'appel bloquant C `ReadFile` sur le handle de la console/pipe verrouille le sous-système d'E/S de la bibliothèque `msvcrt.dll` / `ucrtbase.dll`. Tout appel ultérieur à `print(..., flush=True)` dans le thread principal (y compris les événements `SEGMENT_CHUNK` et `PROGRESS_STREAM`) restait gelé indéfiniment.
- **Conséquence observée** : Le backend Java ne recevait aucun octet, le statut restait bloqué à `PROCESSING 0%`, et aucun son n'était jamais émis tant que le processus ne mourait pas.

### Blocage B : Modèle de transcription monolithique par lots
- **Localisation** : `video-player-backend/scripts/dub_pipeline.py` (antérieurement lignes 420-445).
- **Mécanisme** : Le pipeline accumulait tous les segments renvoyés par Faster-Whisper dans une liste globale avant d'entamer la première traduction et synthèse.
- **Conséquence observée** : Sur une vidéo de 10 minutes, l'utilisateur devait attendre 1 à 2 minutes (la fin de la transcription totale) avant d'entendre la première seconde doublée.

### Blocage C : Buffering des flux standards dans les processus enfants
- **Localisation** : `video-player-backend/src/main/java/com/videoplayer/service/DubService.java` (lignes 400-438).
- **Mécanisme** : Sans l'argument `-u` et la variable d'environnement `PYTHONUNBUFFERED=1`, le runtime Python tamponne automatiquement la sortie standard jusqu'à atteindre un bloc de 4096 octets avant de la transmettre au pipe Java.
- **Conséquence observée** : Les premières répliques restaient captives dans la mémoire tampon du processus enfant.

### Blocage D : Désynchronisation et mutisme dans le lecteur vidéo
- **Localisation** : `video-player-frontend/src/app/components/video-player/video-player.component.ts` (lignes 740-810).
- **Mécanisme** : L'attribution de `currentTime` sur `segmentAudioElement` était exécutée de manière synchrone avant le chargement complet des métadonnées du média (`readyState < HAVE_METADATA`), ce qui provoquait l'abandon silencieux de la position de lecture par le navigateur et un mutisme intermittent.

---

## 3. Analyse des points de blocage : Pourquoi le hors connexion échouait

### Blocage E : Dépendance absolue aux APIs de traduction en ligne
- **Localisation** : `video-player-backend/scripts/dub_pipeline.py` (lignes 170-225).
- **Mécanisme** : La traduction dépendait exclusivement d'appels HTTP sortants vers `translate.googleapis.com` ou `deep_translator.GoogleTranslator`.
- **Conséquence observée** : En l'absence de réseau, l'appel levait une exception `socket.gaierror: [Errno 11001] getaddrinfo failed` ou `TooManyRequests (HTTP 429)`. Aucun moteur hors ligne local (Argos Translate ou NLLB-200) n'était câblé en repli dans le script de streaming.

### Blocage F : Dépendance à Edge-TTS pour la synthèse vocale
- **Localisation** : `video-player-backend/scripts/dub_pipeline.py` (lignes 245-274).
- **Mécanisme** : La synthèse vocale reposait sur la bibliothèque `edge-tts`, qui nécessite une connexion WebSocket permanente avec les serveurs Azure/Bing de Microsoft. Le repli prévu (`gTTS`) nécessitait également une connexion internet.
- **Conséquence observée** : Dès que la connexion internet était coupée, la génération audio échouait immédiatement sans possibilité de bascule sur un moteur de synthèse hors ligne (tel que Piper TTS ONNX ou le moteur vocal local Windows SAPI).

### Blocage G : Architecture séparée non intégrée entre `video-translator` et `video-player`
- **Localisation** : `video-translator/ai-service/main.py` vs `video-player-backend/scripts/dub_pipeline.py`.
- **Mécanisme** : Le projet `video-translator` disposait d'un service IA hors ligne (Argos + Piper dans son dossier `ai-service`), mais ce service était isolé et conçu pour du traitement par lots différé avec réencodage complet de fichier MP4. Il n'était pas relié au lecteur temps réel de `video-player-frontend`.

---

## 4. Plan de résolution ciblé

1. **Intégration d'un moteur de traduction hors ligne local** :
   - Intégrer la cascade de traduction : **Argos Translate (local)** $\rightarrow$ **Cache local** $\rightarrow$ API en ligne (si disponible).
   - Ajout d'une détection proactive des paquets de langues hors ligne avec message d'erreur informatif.
2. **Synthèse vocale hybride avec repli hors ligne garanti** :
   - Utilisation d'Edge-TTS lorsque le réseau est actif pour une qualité studio, avec bascule instantanée sur **Piper TTS local (ONNX)** et les voix locales Windows (SAPI5/System.Speech) en mode déconnecté.
3. **Mise en cache unitaire par empreinte (Fingerprint SHA-256)** :
   - Sauvegarde et réutilisation immédiate de chaque réplique par sa clé cryptographique : relecture instantanée sans recalcul.
4. **Fluidité du lecteur vidéo** :
   - Lancement immédiat de la vidéo (zéro délai de démarrage) en VO ou sourdine, rejoint automatiquement par le flux de segments audio doublés dès réception.
