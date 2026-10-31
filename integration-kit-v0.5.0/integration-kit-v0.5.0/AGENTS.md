# AGENTS.md — Noyau de gouvernance IA (kit v0.5.0)

> Ce fichier est le **seul** fichier à lire systématiquement. Tout le reste se
> charge à la demande via la table de routage (section « Routage »). Ne jamais lire tout le kit
> « par sécurité » : chercher (`rg`, ou `findstr /s /n /i` si rg est absent)
> avant d'ouvrir. Environnement cible : **Windows** (cmd ou PowerShell).

## Standards non négociables (source unique)

Toute violation bloque la review et la release, sauf ADR accepté dans `docs/ai/adr/`.
Contrôle automatique : `scripts\check-standards.bat`.

| # | Règle |
|---|---|
| S1 | Backend : **Java 21**, **Spring Boot 4.1.x**, **Maven uniquement** (`pom.xml`, `mvnw.cmd`) ; versions des dépendances gérées par le BOM Spring Boot. Pas de Gradle. |
| S2 | Spring Boot : **`application.yml`** + profils `application-<profil>.yml`. Pas de nouveau `application.properties`. |
| S3 | Frontend : **Angular 20** (`@angular/*` en `^20`, Node 22 LTS) et **Tailwind CSS v4** CSS-first (`@import "tailwindcss"`, `@theme`). Interdits : Angular Material, Bootstrap, Tailwind v3 (`@tailwind base…`), `tailwind.config.js` comme source de tokens. |
| S4 | Angular : `proxy.conf.json` référencé dans `angular.json` (`proxyConfig`) ; appels API **relatifs** (`/api`), jamais d'URL backend en dur. |
| S5 | UI Angular/Flutter : thème central, **light + dark**, **i18n fr + en** (aucun texte visible en dur), composants réutilisables, config applicative centralisée (nom, logo, URLs publiques). |
| S6 | Design : `DESIGN.md` = source de vérité. Arrondis 4–6 px, **8 px max** ; pas de `rounded-2xl/3xl/full` sur cards, inputs, boutons ; ombres légères. |
| S7 | Architecture : SOLID ; **le backend est maître** de la vérité métier ; couches Controller → Service → Repository, modules Maven core / domaine / web-service ; CRUD via les composants génériques (`AbstractController`, `IAbstractService`, `CrudService`) ; aucune entité JPA hors de la couche service. |
| S8 | Taille : classe/composant/widget ≤ **500 lignes** (alerte 300) ; méthode ≤ 40 lignes ; ≤ 5 paramètres ; complexité ≤ 10. |
| S9 | Sécurité : aucun secret versionné ni loggé ; entrées validées ; SQL paramétré ; AuthZ testée ; PII masquée. |
| S10 | Dépôt : `.gitignore` adapté à la stack (`target/`, `node_modules/`, `dist/`, `build/`, `.dart_tool/`, secrets). |
| S11 | Documentation : commence avec le développement ; une tâche n'est DONE que si la doc impactée est à jour (proportionnée, voir la section « Gabarits »). |
| S12 | Lombok obligatoire et encadré : jamais `@Data` sur une entité JPA ; `@SuperBuilder` dans la hiérarchie Basic/Full ; `@RequiredArgsConstructor` + champs `final` (pas d'`@Autowired` sur un champ) ; `lombok.config` à la racine. Toute erreur API renvoie `ApiResponse` avec un `errorCode` catalogué. |
| S13 | Angular : `@if` / `@for` / `@switch` (jamais `*ngIf`…), `templateUrl` séparé, `inject()`, signaux (`signal`, `input()`, `output()`), `OnPush`, zoneless ; erreurs affichées via une clé i18n dérivée de l'`errorCode`, jamais le message serveur. |

Interdits permanents : supprimer du code sans preuve, changer un contrat API
silencieusement, affaiblir sécurité ou validation, retirer/ignorer des tests,
déclarer DONE sans preuve de test ou justification.

## Format des demandes

Les demandes suivent souvent les modèles de `docs/prompts/PROMPTS-DEV.md` :
des lignes `CLÉ : valeur` en majuscules (`MODE`, `GABARIT`, `TICKET`, `BUG`,
`ERREUR`, `FEATURE`, `CRITÈRES`, `BRANCHE`, `CONSIGNES`…).

- `MODE` et `GABARIT` s'appliquent tels quels (voir « Routage » et « Gabarits »).
- Si une valeur contient encore un emplacement `{{…}}` non remplacé, demander
  la valeur à l'utilisateur au lieu de la supposer.
- Une clé absente n'est pas une erreur : appliquer les règles de ce fichier.

## Qualifier la demande

1. Déterminer le **mode** (tableau de la section « Routage »).
2. Déterminer le **gabarit** : LÉGER ou STANDARD (section « Gabarits »).
3. Si la demande est macro, floue ou > 2–3 jours : ne pas coder, passer en mode
   Project Manager (EPIC → stories → tasks).

## Routage : quoi lire selon le mode

Lire le ticket concerné (`docs/ai/tickets/`) s'il existe, puis **uniquement** :

| Mode | Déclencheur | À charger |
|---|---|---|
| Engineering backend | Spring, API, DB, migration, `pom.xml` | `SKILL.md` ; `docs/standards/BACKEND-SPRING-STANDARDS.md` ; `CONFIGURATION-STANDARDS.md` si configuration |
| Engineering Angular | écran, composant, service HTTP | `SKILL.md` ; `DESIGN.md` ; `docs/standards/FRONTEND-ANGULAR-STANDARDS.md` ; `DESIGN-SYSTEM-STANDARDS.md` si tokens/thème |
| Engineering Flutter | écran, widget, provider | `SKILL.md` ; `DESIGN.md` ; `docs/standards/MOBILE-FLUTTER-STANDARDS.md` |
| Diagnostic | bug incompris, incident | rien de plus au départ ; charger ensuite le standard de la couche en cause |
| QA Review | PR/MR, merge | `docs/qa/QA-REVIEW-WORKFLOW.md` ; `docs/ai/review/BLOQUANTS.md` ; puis seulement les fiches `docs/ai/review/*.md` touchées par le diff (`BACKEND-SPRING.md`, `FRONT-ANGULAR.md`…) |
| Project Manager | cadrage, estimation, sprint, capacité, dérive | `PROJECT-MANAGER-SKILL.md` ; puis le seul doc `docs/pm/` utile |
| Architecture | choix structurant, nouvelle couche, refactoring | `docs/standards/ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md` ; `docs/ai/adr/TEMPLATE-adr.md` ; standard de la technologie concernée |
| Release | version, tag, changelog, hotfix | `docs/release/SEMANTIC-VERSIONING.md` ; `RELEASE-WORKFLOW.md` |
| Config / dépôt | `.gitignore`, proxy, yml | `docs/standards/CONFIGURATION-STANDARDS.md` ou `GITIGNORE-STANDARDS.md` |

Les templates (`docs/**/templates/`, `docs/templates/`) ne s'ouvrent qu'au
moment de créer le fichier correspondant. `docs/ai/REFERENCES.md` seulement pour
une vérification externe. Ne jamais lire `.kit/` (historique du kit) ni `docs/prompts/` (modèles destinés aux humains).

## Gabarits d'intervention

| | LÉGER | STANDARD |
|---|---|---|
| Quand | correctif, petite évolution < 0,5 j, sans changement de contrat API/DB | feature, API, migration, UI significative, sécurité |
| Ticket | `docs/ai/tickets/TEMPLATE-ticket-lite.md` | `docs/ai/tickets/TEMPLATE-ticket-action.md` |
| Doc feature | aucune (sauf si la doc existante devient fausse) | `docs/features/<id>/FUNCTIONAL-SPEC.md` + `TECHNICAL-DESIGN.md` (1 page chacun) ; API/DATA/TEST si impact |
| Suivi | 1 ligne dans `docs/ai/PROJECT-TRACKING.md` | idem + `docs/ai/CHANGELOG.md` si changement notable |
| Réponse finale | format court (section « Réponse finale ») | format court (section « Réponse finale ») + risques |

En cas de doute : STANDARD si le changement touche un contrat (API, DB, auth,
config publique), sinon LÉGER.

## Exécution

- Analyser l'existant par recherche ciblée avant de modifier ; lister impacts
  et risques de régression dans le ticket.
- Changements minimaux, cohérents avec l'existant ; tests ajoutés ou justifiés.
- Vérifications (Windows, une commande par ligne) : `mvnw.cmd test` ;
  `npm run lint`, `npm run test`, `npm run build` ; `flutter analyze`,
  `flutter test` ; `scripts\check-standards.bat .`. Si non exécutées, dire pourquoi.
- Cocher le ticket, mettre à jour `PROJECT-TRACKING.md` (une ligne), le
  changelog si notable, et proposer le bump SemVer si livrable.

## Réponse finale (courte)

Les détails sont dans le ticket ; ne pas les recopier dans le chat.

```markdown
**Résultat** : 1–3 phrases.
**Fichiers modifiés** : liste.
**Vérifications** : commandes + résultat (ou raison si non exécutées).
**Risques / reste à faire** : puces courtes, ou « aucun ».
**SemVer** : aucun / PATCH / MINOR / MAJOR (si livrable).
**Ticket** : lien.
```
