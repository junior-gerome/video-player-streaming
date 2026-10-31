# Revue — Critères bloquants

> Toujours lu en mode QA Review (court). Un seul ❌ ici bloque le merge, sauf ADR accepté.
> Les critères marqués ⚙ sont détectés par `scripts\check-standards.bat` : lancer le script avant la revue manuelle.

| # | Violation | Règle |
|---|---|---|
| B1 | ⚙ Java autre que 21, Spring Boot autre que 4.1.x, Gradle ou nouveau `application.properties` | S1, S2 |
| B2 | ⚙ Angular autre que 20, Angular Material, Tailwind v3 / `tailwind.config.js` | S3 |
| B3 | ⚙ `proxy.conf.json` absent ou non référencé ; URL backend en dur | S4 |
| B4 | UI sans thème central, sans light/dark, avec texte en dur hors i18n `fr`/`en`, ou branding en dur | S5 |
| B5 | UI significative sans `DESIGN.md` ; arrondi supérieur à 8 px sans ADR | S6 |
| B6 | Logique métier dans un contrôleur, une entité ou un composant ; règle critique seulement côté client | S7 |
| B7 | Contrôleur qui appelle un repository (hors `existsById` en validator), du SQL ou un client externe | S7 |
| B8 | Entité JPA exposée par l'API ; CRUD standard réécrit au lieu d'`AbstractController` / `IAbstractService` / `CrudService` | S7 |
| B9 | Dépendance interdite entre modules Maven (domaine → domaine, core → autre module) ou entre domaines `features/` | S7 |
| B10 | ⚙ Classe, composant ou widget de plus de 500 lignes | S8 |
| B11 | Secret versionné ou loggé ; SQL concaténé ; endpoint sensible sans test d'autorisation ; donnée personnelle loggée | S9 |
| B12 | ⚙ `.gitignore` absent ; artefacts de build ou secrets versionnés | S10 |
| B13 | Gabarit STANDARD sans documentation minimale ; migration ou configuration non documentée | S11 |
| B14 | ⚙ `@Data` sur une entité JPA ; Lombok absent du `pom.xml` ; `@Autowired` sur un champ ; `@SneakyThrows` / `val` / `var` Lombok | S12 |
| B15 | Réponse d'erreur sans `errorCode` ; message serveur brut affiché à l'utilisateur ; code d'erreur sans traduction `fr`/`en` | S12, S13 |
| B16 | ⚙ `*ngIf` / `*ngFor` / `*ngSwitch` ; `template:` ou `styles:` en ligne dans un composant | S13 |
| B17 | Test désactivé sans ticket ; couverture sous le seuil défini | — |
| B18 | Changement d'API cassant sans nouvelle version `/api/v{n+1}` ni bump MAJOR | SemVer |

## Fiches par domaine (charger seulement celles touchées par le diff)

| Diff touche… | Fiche |
|---|---|
| Code Spring Boot, `pom.xml`, Lombok | `BACKEND-SPRING.md` |
| Code Angular | `FRONT-ANGULAR.md` |
| Interface Angular ou Flutter (thème, i18n, arrondis) | `FRONT-UI.md` |
| Endpoints, DTO, base, migrations, cache | `API-DATA.md` |
| Découpage, nouvelles couches, abstractions | `ARCHITECTURE.md` |
| Authentification, secrets, en-têtes, mobile | `SECURITE.md` |
| Tests | `TESTS.md` |
| Logs, métriques, performance, pipeline | `OPS.md` |
| Documentation, conformité | `DOC-GOUVERNANCE.md` |
