# Revue — Frontend Angular 20

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.
> Règles : `docs/standards/FRONTEND-ANGULAR-STANDARDS.md`. Thème, i18n, arrondis : `FRONT-UI.md`.

## Socle

- [ ] Paquets `@angular/*` en `^20` ; Node 22 LTS ; Tailwind CSS v4 ; ni Angular Material ni Bootstrap.
- [ ] `provideZonelessChangeDetection()` ; composants en `ChangeDetectionStrategy.OnPush` ; pas de `standalone: true` superflu.

## Organisation

- [ ] Fichiers rangés dans `configurations/`, `core/`, `features/` ou `shared/` (sinon justifié dans la MR).
- [ ] `core/` et `configurations/` n'importent jamais `features/` ; deux domaines de `features/` ne s'importent pas.
- [ ] `shared/` ne contient rien de spécifique à un domaine.
- [ ] Aucun `template:` ni `styles:` en ligne : `templateUrl` obligatoire.
- [ ] Branding et réglages publics dans `configurations/app-settings.ts`, pas dans `app.config.ts`.

## Modèles et services

- [ ] Modèles Basic / Full / Custom et enums identiques aux DTO backend ; FullModel étend BasicModel.
- [ ] Service d'entité étend `CrudService<Basic, Full>` ; endpoint lu dans `configurations/api-endpoints.ts`.
- [ ] Les réponses sont lues via `ApiResponse<T>.data` ; aucune URL absolue.
- [ ] Toute erreur passe par `resolveErrorKey(errorCode)` ; aucun message serveur brut affiché ; chaque code a sa clé `errors.*` en `fr` et `en`.

## Sécurité

- [ ] `AuthService` est une classe abstraite fournie dans `app.config.ts` ; intercepteur et guards n'importent aucun SDK de fournisseur d'identité.
- [ ] Guards fonctionnels (`CanActivateFn`) déclarés sur la route parente du layout.
- [ ] Un 401 déclenche `login()`.

## Composants et templates

- [ ] Dépendances par `inject()` ; entrées/sorties par `input()` / `output()` ; état local en `signal()` et `computed()`.
- [ ] Uniquement `@if` / `@for` / `@switch` (aucun `*ngIf`, `*ngFor`, `*ngSwitch`) ; tout `@for` a un `track`.
- [ ] Chargement de données : `app-skeleton` pendant `isLoading()`, bannière d'erreur traduite sinon ; jamais d'indicateur tournant seul.
- [ ] `@defer` réservé au chargement différé de composants lourds ou secondaires, avec squelette en `@placeholder` / `@loading` et bloc `@error`.
- [ ] Pagination client ou serveur choisie explicitement ; jamais les deux sur un écran.
- [ ] Formulaires réactifs typés (`NonNullableFormBuilder`), création et modification gérées ; paramètre de route reçu en `input()`.
- [ ] Suppressions et actions critiques confirmées par `UtilsService.showPopup()` ; un seul `app-utils-wrapper` dans `app.html`.
- [ ] Écrans chargés à la demande (`loadComponent` / `loadChildren`) sous un layout de persona.

## Nommage

- [ ] Fichiers en kebab-case ; composants sans suffixe (`item-list.ts`), services en `.service.ts`, guards en `.guard.ts`, intercepteurs en `.interceptor.ts`.
- [ ] Sélecteurs préfixés `app-`.

## Tests

- [ ] Services testés avec `HttpTestingController` (cas nominal et `errorCode`).
- [ ] Guards et intercepteur testés en cas autorisé et refusé.
- [ ] Composants testés sur les états chargement, vide, erreur et les interactions.
