# Rapport de Rétro-Ingénierie d'Architecture : VoiceStudio (OmniVoice)

Ce rapport documente l'analyse approfondie du code source du projet **VoiceStudio** (`https://github.com/debpalash/VoiceStudio`, licence **AGPL-3.0**), inspecté directement dans `C:\transcription\VoiceStudio`.
Conformément aux règles de propriété intellectuelle et aux contraintes du projet, **aucun code source n'est copié** : les principes architecturaux, formules et structures de données sont rétro-ingéniés pour guider notre propre implémentation propre (clean-room) en licences compatibles (Apache 2.0 / MIT).

---

## 1. Pipeline exact de doublage

### Fichiers et fonctions sources
- `backend/api/routers/dub_core.py` : Contrôleur REST de haut niveau (`/dub/ingest`, `/dub/transcribe`, `/dub/generate`, `/dub/export`).
- `backend/services/dub_pipeline.py` : Logique métier du pipeline (`find_cached_job`, `publish_rendered_segments`, `source_segments_revision`).
- `backend/services/sonitranslate.py` : Orchestrateur complet alternatif basé sur SoniTranslate (Gradio subprocess sur port 7860).
- `backend/services/segmentation.py` & `backend/services/subtitle_segmenter.py` : Découpage VAD et lissage des segments de sous-titres.

### Ordre séquentiel des étapes
1. **Ingestion & Détection de format** (`dub_core.py` : `ingest_media`) :
   - Validation du média entrant (formats conteneurs vidéo/audio supportés).
   - Extraction de la piste audio maître en PCM 16-bit 16 kHz mono (`ffmpeg -vn -ac 1 -ar 16000`).
   - Calcul du hash SHA-256 du fichier d'entrée (`compute_file_hash`).
2. **Séparation Vocale / Musique de fond** (`services/ffmpeg_utils.py` / `services/sonitranslate.py` / Demucs) :
   - Traitement par lots (batch) sur la totalité du fichier audio source.
   - Production de deux flux : `vocals.wav` (utilisé pour ASR et clonage) et `background.wav` (préservé pour le mixage final avec ducking).
3. **Transcription & VAD (ASR)** (`dub_core.py` : `transcribe_audio`, `services/subprocess_asr.py`) :
   - Découpage par détection d'activité vocale (Silero VAD ou WebRTC VAD) en fenêtres de parole.
   - Transcription horodatée mot à mot et segment par segment via `whisper.cpp` ou Faster-Whisper.
   - Normalisation du texte et ponctuation (`services/text_normalization.py`).
4. **Diarisation des Locuteurs** (`services/transcript_diarization.py` / Sortformer / PyAnnote) :
   - Attribution d'un identifiant locuteur (`speaker_0`, `speaker_1`) à chaque segment.
   - Enregistrement des empreintes acoustiques de référence.
5. **Traduction par segment** (`services/dub_translate.py`, `services/translator.py`) :
   - Envoi du texte segmenté au moteur sélectionné (Argos, NLLB-200, Google, DeepL ou LLM).
   - Conservation des métadonnées de créneau temporel (`start`, `end`, `slot_duration`).
6. **Ajustement de débit & Fit prédictif** (`services/speech_rate.py`, `services/fit_planner.py`) :
   - Calcul de la durée estimée de lecture de la traduction comparée au créneau disponible (`natural_duration vs slot_duration`).
7. **Synthèse vocale (TTS)** (`backend/api/routers/dub_generate.py`, `services/tts_backend.py`) :
   - Rendu audio segment par segment dans le répertoire temporaire `dub/<job_id>/tts/seg_<index>.wav`.
8. **Calage temporel & Mixage** (`services/video_retime.py`, `dub_export.py`) :
   - Application du ratio d'étirement (`WSOLA` ou `atempo` FFmpeg) pour caler l'audio TTS dans son créneau.
   - Superposition sur le fond sonore avec ducking automatique (-9 dB sous la voix).
   - Assemblage et multiplexage final avec la vidéo (`ffmpeg -f concat`).

### Formats d'échange et structures
- Un segment interne est formalisé par :
  ```json
  {
    "id": "seg_00042",
    "index": 42,
    "start": 12.450,
    "end": 16.820,
    "text": "Texte traduit dans la langue cible",
    "source_text": "Original text before translation",
    "speaker_id": "speaker_0",
    "target_lang": "fr",
    "voice_profile": "fr-FR-siwis-medium",
    "speed": 1.0,
    "rate_ratio": 1.08,
    "fingerprint": "a1b2c3d4..."
  }
  ```

---

## 2. Émission et consommation des flux de tâches

### Fichiers et fonctions sources
- `backend/core/tasks.py` : `TaskManager`, `_stream_payload`, `_stream_completed`, `_stream_failure`.
- `backend/core/event_bus.py` : Système de diffusion pub/sub in-process basé sur `asyncio.Queue`.
- `backend/api/routers/events.py` : Point d'entrée WebSocket `/ws/events`.
- `backend/services/inference_cancellation.py` : `InferenceCancellation`, gestion du contexte d'interruption.
- `electron/src/shared/api/` : Clients TypeScript côté interface.

### Protocole et cycle de vie
- **Transport hybride SSE / WebSocket** :
  - **SSE (`/tasks/stream/{id}`)** : Utilisé pour les tâches linéaires longues (ingest, transcription, export). Chaque événement est sérialisé en JSON `data: {"type": "progress"|"chunk"|"done"|"error", ...}`.
  - **WebSocket (`/ws/events`)** : Utilisé pour l'état réactif global. Diffuse des notifications d'invalidation de cache (`{"kind": "jobs", "ts": 1714200000.0}`).
- **Keepalives** :
  - Pour empêcher les coupures des proxies ou du runtime Electron sur les opérations longues sans émission de données, le serveur envoie un ping périodique toutes les 25 secondes : `{"kind": "ping"}`.
- **Règle de clôture stricte (Zero-Silent-Failure)** :
  - Dans `core/tasks.py` (`_stream_failure`), si le flux SSE se termine ou est fermé par le serveur sans qu'un événement explicite `type: "done"` n'ait été reçu, l'opération est automatiquement classée comme un **échec fatal** (`Task failed`).
- **Reprise après coupure (Resume)** :
  - Les événements sont stockés avec un numéro de séquence croissant (`seq`). En cas de reconnexion, le client passe `/tasks/stream/{id}?after_seq=N` pour rattraper les événements manqués sans relancer le calcul.
- **Annulation propagée** :
  - L'objet `InferenceCancellation` utilise un `threading.Event()`. Lorsqu'une annulation est demandée (`POST /dub/abort/{id}`), l'événement est activé, ce qui termine prématurément les boucles d'inférence et ordonne l'arrêt forcé (`kill`) des processus FFmpeg ou sidecars associés via `services/proc_registry.py`.

---

## 3. Empreinte de cache d'un segment et invalidation

### Fichiers et fonctions sources
- `backend/services/incremental.py` : `segment_fingerprint`, `_canon_value`, `_DEFAULT_EFFECT_PRESET`.

### Mécanisme d'empreinte (Deterministic Content Hashing)
L'empreinte d'un segment est un condensat cryptographique déterministe (SHA-256) calculé uniquement à partir des variables influençant directement le résultat sonore :
$$\text{Fingerprint} = \text{SHA256}(\text{text} \parallel \text{target\_lang} \parallel \text{profile\_id} \parallel \text{speed} \parallel \text{direction} \parallel \text{voice\_match} \parallel \text{effect\_preset})$$

### Rôle et propriétés clés
1. **Immunité aux métadonnées neutres** :
   - Un changement d'affichage, de position de curseur dans l'UI ou de badge n'altère pas l'empreinte.
2. **Canonicalisation stricte (`_canon_value`)** :
   - Les valeurs numériques (`speed: 1` vs `speed: 1.0`) sont normalisées en flottants.
   - Les chaînes vides, `None` ou clés absentes sont unifiées en valeurs par défaut afin d'éviter qu'une resoumission de formulaire ne recalcule toute la vidéo.
3. **Invalidation ciblée** :
   - Lors d'une régénération, VoiceStudio compare l'empreinte recalculée avec celle stockée en base/cache. Seuls les segments dont le hash diffère sont réinjectés dans la file TTS. Tous les autres segments sont immédiatement réutilisés sans recalcul.

---

## 4. Prédiction d'ajustement et modes de timing

### Fichiers et fonctions sources
- `backend/services/speech_rate.py` : `expected_duration`, table `_RATE_CPS`, `adjust_for_slot`.
- `backend/services/fit_planner.py` : `plan_fit`, `FitParams`, `MAX_AUDIO_RATE_HARD`.
- `backend/services/video_retime.py` : `execute_video_retime`, gestion du ré-échantillonnage vidéo.

### Table de débit de parole par langue (`_RATE_CPS`)
VoiceStudio calibre la vitesse moyenne de lecture naturelle en nombre de caractères par seconde (CPS) :
- `fr` (Français) : 15.0 CPS
- `en` (Anglais) : 15.0 CPS
- `es` (Espagnol) : 15.5 CPS
- `de` (Allemand) : 14.0 CPS
- `it` (Italien) : 15.0 CPS
- `zh` (Chinois) : 6.0 CPS (caractères logographiques)
- `hi` / `bn` (Indic) : 17.0 CPS

La durée prédite d'une réplique est :
$$\text{Durée\_estimée} = \frac{\text{len(texte)}}{\text{CPS}(\text{langue})}$$

### Modes de gestion temporelle
1. **Strict Slot** :
   - La voix est accélérée via l'algorithme WSOLA ou le filtre FFmpeg `atempo`.
   - Plafond maximal d'accélération audio : $1.4\times$ à $1.8\times$ (`MAX_AUDIO_RATE_HARD`). Au-delà, un avertissement de dépassement est levé et la fin de la réplique est adoucie avec un fondu sortant de 50 ms pour ne jamais émettre de clic audio.
2. **Concise Mode** :
   - Avant de forcer une accélération non naturelle, le texte traduit est reformulé de manière plus concise (élagage des mots de remplissage) pour tenir naturellement dans le créneau.
3. **Smart Fit** :
   - Stratégie hybride à double seuil :
     - Si le dépassement est léger ($\le 1.2\times$) : absorption purement audio par accélération légère.
     - Si le dépassement est supérieur : partage géométrique 50/50 entre une accélération audio modérée et un léger ralentissement vidéo de la scène (`video_ratio`).
4. **Stretch Video** :
   - La vidéo est ralentie sur le segment concerné via le filtre `setpts` pour correspondre à la durée exacte de la voix doublée.

---

## 5. Moteurs de traduction hors ligne et ASR local

### Fichiers et fonctions sources
- `backend/services/translation_engines.py` : Registre `REGISTRY`, sondage `_probe_engine`.
- `backend/services/translator.py` : Stratégies `FastTranslator` et `CinematicTranslator`.
- `backend/engines/whisper_cpp/` : Intégration binaire native Whisper.cpp.

### Moteurs de traduction
1. **Argos Translate (`offline`, local rapide)** :
   - Basé sur OpenNMT et le runtime CTranslate2.
   - Exécution purement CPU ultra-rapide (< 100 ms par réplique).
   - Fonctionne par paquet de langue hors ligne (`.argosmodel`).
2. **NLLB-200 (`offline`, local haute fidélité)** :
   - Modèle `facebook/nllb-200-distilled-600M` exécuté via HuggingFace Transformers / CTranslate2.
   - Poids d'environ 2.4 Go, couverture de 200 langues, traduction de niveau cinématique hors ligne.
3. **Repli ordonné (Fallback Cascade)** :
   - Si un moteur distant (Google/DeepL) est sélectionné mais indisponible (hors ligne ou HTTP 429), le pipeline bascule de manière transparente sur le modèle local installé (Argos ou NLLB).
   - Si aucun modèle local n'est installé pour la paire demandée, une exception bloquante explicite `ModelTermsError("no_offline_engine_for_pair")` est levée immédiatement avec un message clair guidant l'utilisateur vers le téléchargement du paquet de langue.

### ASR Local
- Exécution de `faster-whisper` (modèles `base`, `small`, `medium`) ou `whisper.cpp` en binaire précompilé, interdisant tout appel réseau lors de la transcription.

---

## 6. Modèles TTS locaux et exigences matérielles

D'après `backend/config/models.yaml` et `backend/config/model_licenses.json` :

| Modèle / Moteur | Rôle | Taille | Licence | Exigences CPU / GPU |
| :--- | :--- | :--- | :--- | :--- |
| **OmniVoice TTS** (`k2-fsa/OmniVoice`) | TTS Zero-shot 600+ langues | 2.4 Go | Apache-2.0 | CPU multi-cœur (4+ cœurs) ou GPU CUDA (VRAM $\ge 4$ Go) |
| **audio.cpp bundle** (Breeze-TTS-2) | TTS natif C++ GGUF | 4.98 Go | Apache-2.0 | Très faible latence CPU (AVX2/NEON), 8 Go RAM |
| **Piper TTS ONNX** | TTS mono/multi-locuteurs ultra-rapide | 30–80 Mo / voix | GPL-3.0 / voix MIT-CC0 | Très léger, tourne sur CPU modeste (latence < 150 ms) |
| **Kokoro-82M** | TTS haute qualité compact | ~350 Mo | Apache-2.0 | CPU ou petit GPU, excellente intelligibilité |
| **Faster-Whisper** (`base` / `small`) | Transcription locale ASR | 140 Mo / 460 Mo | MIT | CPU (int8 avec AVX2) ou GPU CUDA |
| **NLLB-200 distilled** | Traduction locale | 2.4 Go | CC-BY-NC-4.0 | RAM $\ge 4$ Go, CPU ou GPU |
| **Argos Translate** | Traduction locale par paquet | ~100 Mo / paire | MIT / CC0 | CPU très modeste (< 200 Mo RAM) |

---

## 7. Analyse comparative : Streaming vs Traitement par lots

| Fonctionnalité dans VoiceStudio | Nature dans VoiceStudio | Adaptabilité au Streaming par fenêtres dans notre projet |
| :--- | :--- | :--- |
| **Extraction audio complète** | Par lots (bloque tout) | **Inutilisable telle quelle** : nous extrayons l'audio par plages horaires (FFmpeg `-ss` / `-t`) ou à la volée. |
| **Séparation vocale Demucs** | Par lots (analyse tout le fichier) | **Incompatible streaming temps réel** : remplacée par un ducking dynamique rapide ou un filtrage fréquentiel sans blocage. |
| **Diarisation PyAnnote** | Par lots (globale) | Remplacée par un profilage de voix par défaut ou un profilage instantané sur les 5 premières secondes. |
| **Calcul d'empreinte unitaire** | Unitaire (`incremental.py`) | **100 % Réutilisable** pour notre cache persistant de segments. |
| **Table de cadence `_RATE_CPS`** | Algorithmique pure | **100 % Réutilisable** pour calibrer la durée des créneaux avant TTS. |
| **Fit audio `atempo` borné** | Unitaire par segment | **100 % Réutilisable** pour caler la réplique dans son créneau temporel. |
| **Émission d'événements keepalive** | WebSocket / SSE | **100 % Réutilisable** pour maintenir le lecteur connecté sans timeout. |
| **Annulation via Scope** | Par tâche (`InferenceCancellation`) | **100 % Réutilisable** pour couper les segments périmés lors d'un Seek. |

---

## 8. Synthèse pour notre architecture

VoiceStudio prouve la viabilité des briques locales (Argos, Faster-Whisper, Piper, tables CPS, hashing unitaire), mais confirme son statut d'outil d'export studio en différé.
Pour notre projet de lecteur vidéo interactif, nous retenons :
1. Le **hashing unitaire de segment** pour le cache persistant.
2. Le **moteur de traduction local Argos / NLLB** avec bascule automatique.
3. Le **modèle de fit prédictif** basé sur la table CPS.
4. Un **ordonnanceur temps réel glissant** (absent de VoiceStudio) piloté par la tête de lecture du lecteur vidéo HTML5/Web Audio.
