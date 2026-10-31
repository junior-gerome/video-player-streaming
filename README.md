# 🎬 Video Player — Angular + Spring Boot

Lecteur vidéo complet avec gestion de playlist, upload et streaming.

## Architecture

| Couche      | Technologie                          |
|-------------|--------------------------------------|
| Frontend    | Angular 17 + Tailwind CSS            |
| Backend     | Spring Boot 3.2 (Java 21) + Gradle   |
| Base de données | MySQL 8 (WAMP local)              |
| Stockage    | MinIO (S3-compatible, Docker)        |

## Fonctionnalités

- 📁 Upload de vidéos (drag & drop, progression)
- ▶️ Lecteur vidéo custom (play/pause, volume, seek, vitesse, plein écran)
- 📜 Gestion de playlists (création, ajout/suppression de vidéos)
- 🔍 Recherche et pagination
- ⏩ Streaming avec support des requêtes Range (seeking)

## Prérequis

- [Docker](https://www.docker.com/) (pour MinIO)
- [JDK 17+](https://adoptium.net/)
- [Node.js 18+](https://nodejs.org/)
- MySQL 8 accessible sur `localhost:3307`

> ⚠️ **Base de données** : le projet est configuré pour utiliser un **MySQL local** (par ex. WAMP) sur le port `3307` avec l'utilisateur `root` **sans mot de passe**. Adaptez `application.yml` si besoin.

## Démarrage

### 1. Lancer MySQL (WAMP local)

Démarrez WAMP/MySQL — le serveur doit écouter sur `3307` avec `root` sans mot de passe, ou adaptez la config dans `application.yml`.

### 2. Lancer MinIO (Docker)

```bash
docker compose up -d minio
```

MinIO : API sur `localhost:9000`, console sur `localhost:9001` (user: `minioadmin` / `minioadmin`).

### 3. Lancer le backend Spring Boot

```bash
cd video-player-backend
gradlew.bat bootRun
```

L'API est disponible sur `http://localhost:8080`. La base `video_player` et les tables sont créées automatiquement.

### 4. Lancer le frontend Angular

```bash
cd video-player-frontend
npm install
ng serve
```

L'application est disponible sur `http://localhost:4200`.

## API REST

### Vidéos
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/videos/upload` | Uploader une vidéo (multipart: file, title, description) |
| `GET` | `/api/videos` | Lister les vidéos (page, size) |
| `GET` | `/api/videos/{id}` | Détails d'une vidéo |
| `GET` | `/api/videos/{id}/stream` | Streamer une vidéo (support Range) |
| `GET` | `/api/videos/search?q=` | Rechercher des vidéos |
| `DELETE` | `/api/videos/{id}` | Supprimer une vidéo |

### Playlists
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/playlists` | Créer une playlist |
| `GET` | `/api/playlists` | Lister les playlists |
| `GET` | `/api/playlists/{id}` | Détails d'une playlist |
| `PUT` | `/api/playlists/{id}` | Mettre à jour une playlist |
| `PUT` | `/api/playlists/{id}/videos` | Ajouter des vidéos |
| `DELETE` | `/api/playlists/{id}/videos/{videoId}` | Retirer une vidéo |
| `DELETE` | `/api/playlists/{id}` | Supprimer une playlist |

## Configuration

Dans `video-player-backend/src/main/resources/application.yml` :

```yaml
# MySQL local (WAMP)
spring.datasource.url: jdbc:mysql://localhost:3307/video_player?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username: root
spring.datasource.password: ""

# MinIO
minio.endpoint: http://localhost:9000
minio.access-key: minioadmin
minio.secret-key: minioadmin
minio.bucket: videos
```