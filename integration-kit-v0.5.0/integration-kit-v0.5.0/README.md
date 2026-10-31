# AI Engineering + Scrum Governance Kit — v0.5.0

Kit de gouvernance pour les projets **Java 21 / Spring Boot 4.1 (Maven, Lombok) / Angular 20 (Tailwind v4) / Flutter**
avec intervention d'assistants IA (Claude Code, Codex, Gemini CLI, Copilot…).

Objectif : tickets clairs, pas de régression, standards respectés, livraisons
prouvées, **avec une consommation de tokens maîtrisée**.

## Principe : un noyau, puis des modules à la demande

| Niveau | Contenu | Chargé |
|---|---|---|
| Noyau | `AGENTS.md` : standards S1–S13, routage, gabarits, format de réponse | Toujours (≈ 2 200 tokens) |
| Module | Skill ou standard du mode (ex. `BACKEND-SPRING-STANDARDS.md`) | Selon le mode |
| Référence | Templates, fiches de revue, références, archives | Ponctuellement |

Coût typique d'une intervention : **2 000 à 7 000 tokens** de règles (contre
≈ 35 000 en v0.3.8). Mesure : `scripts\kit-budget.bat`.

## Contenu

```text
AGENTS.md                     noyau unique (source des standards)
CLAUDE.md / GEMINI.md         adaptateurs : @AGENTS.md
SKILL.md                      méthode d'ingénierie
PROJECT-MANAGER-SKILL.md      cadrage, estimation, Scrum, delivery
VERSION
scripts\
  check-standards.bat         contrôle automatique des standards (poste et CI)
  kit-budget.bat              coût en tokens par profil de lecture
docs\
  prompts\   modèles de demandes prêts à l'emploi (pour les humains)
  ai\        tickets, suivi, changelog projet, ADR, fiches de revue, archive
  pm\        Scrum, capacité, estimation, profils, templates
  qa\        workflow de revue
  release\   SemVer, workflow de release, matrice de versions
  standards\ backend Spring, frontend Angular, mobile Flutter, architecture commune,
             configuration, design system, arrondis, gitignore, documentation
  templates\ Angular, Flutter, Spring, design, documentation, gitignore, CI
.kit\                         historique du kit (jamais lu par l'IA)
```

## Standards par technologie

| Technologie | Standard | Fiche de revue | Modèles |
|---|---|---|---|
| Java 21 / Spring Boot 4.1 / Lombok | `docs/standards/BACKEND-SPRING-STANDARDS.md` | `docs/ai/review/BACKEND-SPRING.md` | `docs/templates/spring/` |
| Angular 20 / Tailwind v4 | `docs/standards/FRONTEND-ANGULAR-STANDARDS.md` | `docs/ai/review/FRONT-ANGULAR.md` | `docs/templates/angular/` |
| Flutter | `docs/standards/MOBILE-FLUTTER-STANDARDS.md` | `docs/ai/review/FRONT-UI.md` | `docs/templates/flutter/` |
| Principes communs | `docs/standards/ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md` | `docs/ai/review/ARCHITECTURE.md` | — |

## Modes d'intervention

Engineering (backend / Angular / Flutter) · Diagnostic · QA Review ·
Project Manager · Architecture · Release · Config/dépôt. Le mode détermine les
fichiers chargés (`AGENTS.md`, section « Routage »).

## Gabarits

- **LÉGER** : correctif ou évolution < 0,5 j sans changement de contrat → ticket
  court, pas de spécification.
- **STANDARD** : feature, API, migration, UI significative, sécurité → ticket
  complet + `docs/features/<id>/`.

## Demandes à l'IA

Les modèles prêts à l'emploi sont dans `docs/prompts/PROMPTS-DEV.md`
(20 modèles). Copier un modèle, remplacer chaque `{{…}}`, ne pas modifier les
mots-clés en majuscules (`MODE :`, `GABARIT :`…). Exemple :

```text
MODE : Engineering backend
GABARIT : LÉGER
TICKET : {{ID_TICKET}}
BUG : {{SYMPTOME_OBSERVE}}
ERREUR : {{PREMIERE_EXCEPTION_ET_CAUSED_BY | aucune}}
ATTENDU : {{COMPORTEMENT_ATTENDU}}
CONSIGNES : cherche avec rg avant d'ouvrir les fichiers ; ajoute un test ; réponse au format court.
```

## Installation

Voir `INSTALLATION.md` (Windows : installation neuve, projet existant, migration).
