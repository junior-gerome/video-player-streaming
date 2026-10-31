# video-translator — doublage vidéo multilingue 100 % open source

Envoie une vidéo, choisis des langues, récupère un MP4 contenant la piste audio
d'origine + une piste doublée par langue (sélectionnable dans VLC, YouTube Studio, etc.).

```
Angular 18 ──► Spring Boot 3 (Java 21) ──► ai-service (FastAPI)
  upload/suivi    jobs async + FFmpeg         faster-whisper → Argos Translate → Piper
```

Flux : extraction audio (FFmpeg) → transcription **une seule fois** avec horodatage
→ pour chaque langue : traduction par segment, voix par segment, calage sur la
timeline (accélération jusqu'à x1.6) → fusion en pistes audio dans le MP4.

## Démarrage

```bash
docker compose up --build        # ai-service :8000 + backend :8080
```
Frontend : dans un projet Angular 18, copiez `frontend/src/app/*`, ajoutez
`provideHttpClient()` dans `app.config.ts`, puis `ng serve` (CORS autorisé sur :4200).

Le premier doublage vers une langue télécharge les paquets Argos (internet requis une fois,
ensuite tout fonctionne hors ligne grâce au volume `argos-data`).

## Composants et licences (à re-vérifier avant toute redistribution)

| Brique | Rôle | Licence |
|---|---|---|
| faster-whisper / modèles Whisper | Transcription | MIT |
| Argos Translate | Traduction hors ligne | MIT ou CC0 |
| Piper (OHF-Voice/piper1-gpl) | Synthèse vocale | GPL-3.0 (appelé en processus séparé) |
| Voix Piper | Voix | **Varie selon la voix** : vérifiez chaque modèle |
| FFmpeg | Audio/vidéo | LGPL/GPL selon le build |
| Ce projet | | MIT (voir LICENSE) |

À éviter pour un projet ouvert et réutilisable : XTTS (licence non commerciale)
et NLLB-200 (CC-BY-NC), d'après mes informations à vérifier.

## Limites connues
- Voix Piper : naturelles mais sans clonage de la voix d'origine ; une seule voix par langue.
- Pas de séparation des locuteurs ni de la musique de fond.
- Les langues cibles sont celles de `VOICES` dans `ai-service/main.py` (ajoutez une voix + son code).
- Qualité de traduction Argos < modèles LLM/DeepL, surtout pour l'arabe et les langues pivotées par l'anglais.
- Jobs gardés en mémoire (perdus au redémarrage) : à remplacer par JPA pour la production.
