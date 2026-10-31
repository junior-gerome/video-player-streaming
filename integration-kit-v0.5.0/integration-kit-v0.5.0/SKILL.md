---
name: production-ready-ai-engineering
description: Méthode d'ingénierie Java 21 / Spring Boot 4.1 / Angular 20 / Flutter. À charger en mode Engineering (voir AGENTS.md, section « Routage »). Les standards non négociables sont dans AGENTS.md, section « Standards non négociables », et ne sont pas répétés ici.
---

# Skill — Ingénierie production-ready

## 1. Posture

Raisonner en ingénieur senior responsable du produit, pas en exécutant.
Une tâche est terminée quand : existant analysé, impacts identifiés,
changement minimal et justifié, tests ajoutés ou justifiés, vérifications
listées, suivi mis à jour.

## 2. Avant de modifier

Inspecter **par recherche ciblée**, uniquement ce que le changement touche :
architecture et tests existants, contrats API, modèle de données et
migrations, sécurité, configuration, logs, CI/CD.

## 3. Standard de la technologie touchée

Charger **un seul** standard, celui de la couche modifiée :

| Couche | Standard | Points à ne jamais oublier |
|---|---|---|
| Backend | `docs/standards/BACKEND-SPRING-STANDARDS.md` | composants génériques du core-module, DTO Basic/Full/Custom, validation nullité → existence → métier, `@Transactional` sur les services, Lombok encadré, `ApiResponse` avec `errorCode` |
| Angular | `docs/standards/FRONTEND-ANGULAR-STANDARDS.md` | modèles alignés sur les DTO, `CrudService`, signaux, `@if`/`@for`, squelettes, erreurs en clés i18n, Tailwind v4 sur tokens |
| Flutter | `docs/standards/MOBILE-FLUTTER-STANDARDS.md` | feature-first, thème light/dark, ARB `fr`/`en`, stockage sécurisé |

Nouvelle couche, découpage ou refactoring : ajouter
`docs/standards/ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md`.

## 4. Sécurité (compléments de S9)

CORS explicite ; dépendances scannées en CI ; fichiers uploadés validés en
type, taille et contenu ; endpoints sensibles couverts par des tests AuthN/AuthZ.

## 5. Tests et vérifications

```bat
REM Backend (build complet ou CI impacté : mvnw.cmd clean verify)
mvnw.cmd test
REM Angular
npm run lint
npm run test
npm run build
REM Flutter
flutter analyze
flutter test
REM Standards du kit
scripts\check-standards.bat .
```

Si non exécutés, le dire et expliquer pourquoi.

## 6. Versioning

`fix:` → PATCH · `feat:` → MINOR · `BREAKING CHANGE` / `!` → MAJOR.
Aucun changement cassant caché dans un PATCH ou un MINOR. Migration DB
destructive, changement de contrat API, d'auth ou de comportement métier :
envisager MAJOR. Procédure : `docs/release/SEMANTIC-VERSIONING.md`.

## 7. Économie de contexte

- `rg -n "<symbole>" src` (ou `findstr /s /n /i "<symbole>" src\*`) avant d'ouvrir un fichier ; lire des plages de
  lignes plutôt que des fichiers entiers.
- Ne pas recopier le code modifié dans la réponse : le diff suffit.
- Un seul standard chargé à la fois, celui de la couche touchée.
