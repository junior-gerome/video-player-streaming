# Changelog

Tous les changements notables du projet doivent être documentés ici.

Le format suit l'esprit de Keep a Changelog et le versioning suit Semantic Versioning.

## [Unreleased]

## [0.5.0] - 2026-09-29

### Added

- `docs/standards/BACKEND-SPRING-STANDARDS.md` : standard d'équipe backend
  (modules Maven core / domaine / web-service, composants génériques
  `AbstractController` / `IAbstractService`, DTO Basic / Full / Custom,
  exceptions catégorisées), socle Java 21 / Spring Boot 4.1, section Lombok.
- `docs/standards/FRONTEND-ANGULAR-STANDARDS.md` : standard d'équipe Angular 20
  (Signals, `CrudService`, zoneless, contrôle de flux, squelettes, i18n des
  erreurs, Tailwind v4 sur tokens).
- Fiches de revue `BACKEND-SPRING.md` et `FRONT-ANGULAR.md` (listes de contrôle
  des deux standards, déplacées hors des standards).
- Règles S12 (Lombok, `ApiResponse` avec `errorCode`) et S13 (conventions Angular).
- `check-standards.bat` : contrôles Java 21, Spring Boot 4.1.x, Angular 20,
  Lombok (`@Data` sur entité, `@Autowired` sur champ, fonctions interdites,
  `lombok.config`), `*ngIf` / `*ngFor`, templates en ligne.
- Modèles Spring : `pom-java21-lombok.xml.example`, `lombok.config.example`,
  catalogue `TEMPLATE-ERROR-CODES.md`.
- 11 modèles Angular compilés en mode strict (`crud.service`, `auth`,
  intercepteur, guard, `app.config`, routes, liste…).
- Modèles de prompts D18 (entité CRUD), D19 (écrans Angular), D20 (montée de socle).

### Changed

- `ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md` réduit aux principes communs ;
  généricité CRUD du core-module reconnue.
- `FRONTEND-MOBILE-STANDARDS.md` remplacé par `MOBILE-FLUTTER-STANDARDS.md`
  (la partie Angular est dans le standard Angular).
- Fiches de revue réécrites en français et alignées (plus de `record`
  obligatoire, plus de RFC 9457, plus d'Angular 22 / Spring Boot 4.0).
- Tokens Tailwind : `rounded-md` 6 px, `rounded-lg` et `rounded-xl` 8 px ;
  couleurs `success` et `warning` ajoutées.
- Branding Angular déplacé de `app.config.ts` vers
  `configurations/app-settings.ts` ; fichiers publics dans `public/`.
- `SKILL.md`, `CONFIGURATION-STANDARDS.md`, `VERSION-MATRIX.md`,
  `TEMPLATE-TECHNICAL-DESIGN.md`, `REFERENCES.md` alignés.

### Removed

- `docs/templates/spring/ARCHITECTURE-PACKAGE-STRUCTURE.md` (remplacé par le
  standard backend).

## [0.4.1] - 2026-09-29

### Changed

- Scripts portés pour Windows : `check-standards.bat` et `kit-budget.bat`
  (Batch + PowerShell intégré, aucune installation ; exécutables aussi par
  PowerShell 7 sur un runner Linux). Détection des secrets étendue aux
  fichiers `.properties`.
- Commandes des documents adaptées à Windows (`mvnw.cmd`, une commande npm
  par ligne, `findstr` en alternative à `rg`).
- Modèles CI : GitHub Actions `windows-latest`, GitLab runner Windows (et
  variante Linux via PowerShell 7).
- Références par numéro de section (« § ») remplacées par le nom des sections ;
  titres d'`AGENTS.md` dénumérotés.
- `INSTALLATION.md` réécrit pour Windows (nouveau projet, projet existant, migration).

### Added

- `docs/prompts/PROMPTS-DEV.md` : 17 modèles de demandes prêts à l'emploi
  (mots-clés en majuscules, éléments à remplacer entre `{{…}}`).
- Section « Format des demandes » dans `AGENTS.md`.

### Removed

- `scripts/check-standards.sh`, `scripts/kit-budget.py`.

## [0.4.0] - 2026-09-29

### Changed — Réduction de la consommation de tokens

- `AGENTS.md` devient un noyau unique (≈ 1 700 tokens) : standards S1–S11 en une
  ligne chacun, table de routage mode → modules, gabarits LÉGER / STANDARD,
  réponse finale courte. Lecture obligatoire ramenée d'environ 35 000 à
  2 000 – 6 000 tokens selon le mode.
- `CLAUDE.md` et `GEMINI.md` réduits à `@AGENTS.md` (les 15 imports Gemini,
  ≈ 33 000 tokens par session, sont supprimés).
- Suppression des blocs recopiés (SOLID, `.gitignore`, arrondis, Maven,
  Tailwind, configuration, Documentation First) dans les skills, docs PM,
  standards, release et templates : une seule source par règle.
- `SKILL.md` recentré sur la méthode d'ingénierie ; `PROJECT-MANAGER-SKILL.md`
  allégé et numérotation corrigée.
- `review-checklist.md` (≈ 6 000 tokens) scindé en `docs/ai/review/` :
  `BLOQUANTS.md` + 7 fiches par domaine chargées selon le diff.
- `WORKFLOW-IA.md`, `README-IA.md`, `QA-REVIEW-WORKFLOW.md` réécrits en version compacte.
- `PROJECT-TRACKING.md` : 10 colonnes au lieu de 19, archivage par sprint dans `docs/ai/archive/`.
- Templates de tâche/story/epic/sprint : règles recopiées remplacées par une section « Conformité ».

### Added

- Gabarit LÉGER et `docs/ai/tickets/TEMPLATE-ticket-lite.md`.
- `scripts/check-standards.sh` : contrôle automatique des standards S1–S4, S6,
  S8–S10 avec exceptions par ADR (`.standards-exceptions`).
- `scripts/kit-budget.py` : estimation du coût en tokens par profil, garde-fou
  sur la taille du noyau.
- Exemples CI GitHub Actions et GitLab CI (`docs/templates/ci/`).

### Removed

- Historique du kit (changelog, TICKET-0001) sorti des fichiers projet et
  déplacé dans `.kit/`, jamais lu par l'IA.


## [0.3.8] - 2026-07-02

### Added

- Ajout d’un `.gitignore` racine standard pour projets Spring Boot / Angular / Flutter.
- Ajout du standard `docs/standards/GITIGNORE-STANDARDS.md`.
- Ajout des templates `.gitignore` par stack :
  - Spring Boot Maven ;
  - Angular Tailwind CSS v4 ;
  - Flutter ;
  - projet combiné Spring + Angular + Flutter.

### Changed

- Renforcement des workflows IA, QA et templates tickets pour vérifier le `.gitignore`.
- Ajout d’une règle empêchant de versionner secrets, caches, artefacts de build et fichiers locaux.




## [0.3.7] - 2026-07-02

### Added

- Ajout du standard `docs/standards/ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md`.
- Ajout des règles strictes SOLID, séparation des responsabilités et backend maître.
- Ajout de règles sur services/use cases, interfaces/implémentations, généricité, héritage et abstractions.
- Ajout du template `docs/templates/spring/ARCHITECTURE-PACKAGE-STRUCTURE.md`.

### Changed

- Renforcement des workflows IA, QA, tickets et templates Scrum pour contrôler la logique métier, la taille des classes et la séparation backend/front.



## [0.3.6] - 2026-07-02

### Added

- Ajout du standard `docs/standards/UI-RADIUS-AND-SHADOW-STANDARDS.md`.
- Ajout d’une règle stricte sur les arrondis sobres pour Angular et Flutter.
- Ajout d’une règle de profondeur par ombres légères plutôt que par arrondis excessifs.
- Mise à jour des templates design pour documenter les tokens de radius et shadows.

### Changed

- Renforcement des workflows IA, QA et design pour bloquer les interfaces trop arrondies sans justification.



## [0.3.5] - 2026-07-02

### Added

- Ajout du standard obligatoire **Google DESIGN.md + Tailwind CSS v4** pour les interfaces Angular et Flutter.
- Ajout de `docs/standards/DESIGN-SYSTEM-STANDARDS.md`.
- Ajout de `docs/templates/design/DESIGN.md.example`.
- Ajout du template Angular `tailwind-v4-theme.css.example` basé sur `@theme`.
- Ajout des contrôles design system dans les tickets, stories, tasks, QA, release et documentation technique.

### Changed

- Remplacement de l’ancien exemple Tailwind v3 par une approche Tailwind CSS v4 CSS-first.
- Renforcement des règles de review : `DESIGN.md` obligatoire pour toute UI significative.
- Interdiction explicite de Tailwind v3, `@tailwind base/components/utilities` et `tailwind.config.js` comme source de tokens applicatifs sauf ADR.
- Extension du workflow UI pour vérifier tokens, contrastes, light/dark, i18n, composants réutilisables et mapping Flutter.


## [0.3.4] - 2026-07-02

### Added

- Ajout du standard obligatoire **Documentation First** : documentation fonctionnelle et technique dès le démarrage du développement.
- Ajout de `docs/standards/DOCUMENTATION-FIRST.md`.
- Ajout des templates de documentation : functional spec, technical design, API contract, data model, test plan et user guide.

### Changed

- Renforcement des Definition of Ready / Definition of Done avec documentation obligatoire.
- Mise à jour des workflows IA, Scrum, QA et release pour bloquer les changements non documentés.
- Ajout de la charge documentaire dans la capacité sprint.


## [0.3.3] - 2026-07-02

### Added

- Ajout des standards obligatoires Frontend/Mobile : thème centralisé, composants réutilisables, internationalisation FR/EN, thèmes light/dark et configuration applicative centralisée.
- Ajout de `docs/standards/FRONTEND-MOBILE-STANDARDS.md`.
- Ajout de templates Angular pour `app.config.ts`, thème, Tailwind et fichiers i18n.
- Ajout de templates Flutter pour `AppConfig`, thème, contrôleur de thème et fichiers l10n/ARB.

### Changed

- Renforcement des checklists de review avec des critères bloquants UI/i18n/branding.
- Mise à jour des templates PM pour cadrer les impacts thème, i18n, composants et configuration applicative.

## [0.3.2] - 2026-07-02

### Added

- Ajout du standard obligatoire **Spring Boot configuration YAML** : `application.yml` et profils `application-<profile>.yml`.
- Ajout du standard obligatoire **Angular dev proxy** : `proxy.conf.json` versionné et référencé dans `angular.json`.
- Ajout de templates d’exemple pour `application.yml` et `proxy.conf.json`.

### Changed

- Renforcement des workflows, checklists, templates PM/QA/release pour bloquer `application.properties`, l’absence de proxy Angular et les URLs backend hardcodées côté frontend.



## [0.3.1] - 2026-07-02

### Changed

- Ajout du standard obligatoire **Maven uniquement** pour les backends Spring Boot.
- Ajout du standard obligatoire **Tailwind CSS v4 uniquement** pour les frontends Angular.
- Interdiction d’Angular Material sauf exception documentée par ADR.
- Mise à jour des workflows, checklists, templates Scrum/PM, QA et release pour appliquer ces standards.


### Added

- Mise en place d'une gouvernance IA centralisée.
- Ajout d'un workflow obligatoire pour Codex, Gemini, Claude Code et autres IA.
- Ajout d'un modèle de ticket actionnable avec cases à cocher.
- Ajout d'un fichier de suivi global `PROJECT-TRACKING.md`.
- Ajout d'un fichier de références techniques.
- Ajout d'une couche Chef de projet / Scrum / Delivery.
- Ajout d'un guide de capacité sprint.
- Ajout d'un guide d'estimation par story points et profils.
- Ajout d'un modèle de dashboard delivery.
- Ajout de templates Epic, User Story, Task, Sprint Plan, Timesheet et Weekly Report.

### Changed

- Renforcement du `SKILL.md` avec une règle anti-ticket isolé.
- Extension explicite aux projets Spring Boot, Angular et Flutter.
- Extension du template de ticket avec estimation, profil recommandé, sprint, reviewer et suivi du temps.

### Security

- Ajout de références obligatoires OWASP Top 10, OWASP ASVS, OWASP MASVS et OWASP API Security.

## [0.3.0] - 2026-07-01

### Added

- Ajout de la gouvernance Semantic Versioning.
- Ajout du fichier racine `VERSION`.
- Ajout de `docs/release/SEMANTIC-VERSIONING.md`.
- Ajout de `docs/release/RELEASE-WORKFLOW.md`.
- Ajout de `docs/release/VERSION-MATRIX.md`.
- Ajout des templates de release note et décision de version.

### Changed

- Mise à jour des instructions IA pour imposer l'analyse PATCH / MINOR / MAJOR.
- Extension des templates ticket, epic, story, task et sprint avec l'impact version.

## [0.2.0] - 2026-07-01

### Added

- Version complète Engineering + Scrum Governance.

## [0.1.0] - 2026-06-29

### Added

- Première version du kit documentaire IA.
