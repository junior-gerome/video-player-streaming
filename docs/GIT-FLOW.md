# Guide Git Flow & Déploiement

Ce dépôt respecte le standard **Git Flow** pour assurer la qualité, la traçabilité et la stabilité des versions.

---

## 1. Structure des branches

| Branche | Rôle | Politique de merge |
| :--- | :--- | :--- |
| `main` | Branche de production principale. Contient le projet complet stable et déployable. | Merge uniquement depuis `develop` ou `hotfix/*` avec tag de version (ex: `v1.0.0`). |
| `develop` | Branche d'intégration continue des fonctionnalités (monorepo complet). | Reçoit les branches `feature/*` après validation des tests. |
| `frontend` | **Branche dédiée Frontend** : isolée uniquement sur l'application Angular (`video-player-frontend`). | Permet le développement et déploiement autonome de l'interface utilisateur. |
| `backend` | **Branche dédiée Backend** : isolée uniquement sur l'application Spring Boot & pipelines Python (`video-player-backend`). | Permet le développement et déploiement autonome des microservices et jobs IA. |
| `feature/streaming-offline-dubbing` | Branche de développement de la fonctionnalité de doublage multilingue en streaming temps réel et traduction 100% hors-ligne. | Mergée dans `develop` via Pull Request / revue. |
| `hotfix/*` | Correctifs d'urgence directement appliqués sur `main` puis reportés sur `develop`. | Merge dans `main` et `develop`. |

---

## 2. Fonctionnalités incluses (`feature/streaming-offline-dubbing`)

1. **Doublage Streaming Zéro-Délai** :
   - Démarrage instantané de la vidéo sans attente de pré-calcul.
   - Génération audio par segments au fil de l'eau (`SEGMENT_CHUNK`) synchronisés via SSE / WebSockets.
   - Tampon glissant prédictif de 20 à 30 secondes visualisé en temps réel sur la timeline.

2. **Traduction & Synthèse 100% Hors-Ligne** :
   - Pré-téléchargement et extraction en un clic des packages `.argosmodel` (Argos Translate).
   - Inférence locale SentencePiece / CTranslate2 sans dépendance cloud ni clé API.
   - Synthèse vocale locale native (Windows SAPI5) avec gestion d'accents multi-voix.

3. **Cache Déterministe SHA-256** :
   - Indexation automatique des segments audio générés pour relecture instantanée sans recalcul.

---

## 3. Commandes de travail Git Flow

```bash
# Basculer sur la branche de développement
git checkout develop

# Créer une nouvelle branche de fonctionnalité
git checkout -b feature/ma-nouvelle-feature develop

# Fusionner la feature dans develop après tests
git checkout develop
git merge --no-ff feature/ma-nouvelle-feature

# Préparer une release pour main
git checkout main
git merge --no-ff develop
git tag -a v1.0.0 -m "Release v1.0.0 : Doublage streaming et traduction hors-ligne"
git push origin main develop --tags
```
