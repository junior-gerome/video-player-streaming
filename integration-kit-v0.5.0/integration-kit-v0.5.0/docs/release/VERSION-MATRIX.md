# VERSION MATRIX — Versions par module

> À utiliser surtout en monorepo ou si backend, web et mobile ne sortent pas toujours ensemble.

## Version globale projet

| Champ | Valeur |
|---|---|
| Version courante | 0.3.0 |
| Dernière release | 2026-07-01 |
| Stratégie | SemVer |
| Source de vérité | `VERSION` + `docs/ai/CHANGELOG.md` |

## Modules

| Module | Version | Type de version | Dernière release | Compatibilité | Notes |
|---|---:|---|---|---|---|
| Backend Spring Boot | 0.3.0 | SemVer | 2026-07-01 | API v0 | À adapter au projet |
| Angular Web | 0.3.0 | SemVer | 2026-07-01 | API v0 | À adapter au projet |
| Flutter Mobile | 0.3.0 | SemVer | 2026-07-01 | API v0 | À adapter au projet |
| API Contract | 0.3.0 | SemVer | 2026-07-01 | v0 | À adapter au projet |
| Database Schema | 0.3.0 | Migration versionnée | 2026-07-01 | v0 | Flyway/Liquibase recommandé |

## Compatibilité API / clients

| API version | Backend min | Angular min | Flutter min | Statut | Fin support |
|---|---:|---:|---:|---|---|
| v0 | 0.3.0 | 0.3.0 | 0.3.0 | Active | À définir |

## Règles

- Si backend et clients sortent ensemble, garder une version globale unique.
- Si les clients mobile/web peuvent rester sur d'anciennes versions, maintenir cette matrice.
- Toute rupture API doit créer une ligne de compatibilité ou un bump MAJOR.
- Toute migration destructive doit être documentée dans une release note et/ou un ADR.

## Socle technique

Les versions de plateforme ne se gèrent pas ici : elles sont fixées par les
standards (`BACKEND-SPRING-STANDARDS.md` : Java 21, Spring Boot 4.1 ;
`FRONTEND-ANGULAR-STANDARDS.md` : Angular 20, Node 22 LTS) et contrôlées par
`scripts\check-standards.bat`.
