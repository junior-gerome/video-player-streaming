# Standard Frontend — Angular 20 LTS / Signals / Tailwind CSS v4

> Standard d'équipe du frontend Angular, pendant du standard backend
> (`BACKEND-SPRING-STANDARDS.md`) : les modèles reprennent le nommage des DTO.
> Chargé en mode Engineering Angular, avec `DESIGN.md`.
> Listes de contrôle de revue : `docs/ai/review/FRONT-ANGULAR.md`.
> Tokens visuels : `DESIGN-SYSTEM-STANDARDS.md` ; arrondis et ombres :
> `UI-RADIUS-AND-SHADOW-STANDARDS.md`.

## Socle technique

| Élément | Version de référence | Règle |
|---|---|---|
| Angular | **20.x (LTS)** | tous les paquets `@angular/*` en `^20` |
| Node.js | 22 LTS | version fixée dans `.nvmrc` |
| TypeScript | version exigée par Angular 20 | installée par le CLI, jamais forcée à la main |
| Tailwind CSS | v4, approche CSS-first | `@import "tailwindcss"` + `@theme` |
| RxJS | 7.8 | uniquement pour le HTTP et les flux ; l'état est en Signals |
| i18n | chargement à l'exécution, `fr` et `en` | `@ngx-translate/core`, pipe `translate` |

- Composants **standalone** (défaut d'Angular 20 : ne pas écrire `standalone: true`).
- Détection de changements : `provideZonelessChangeDetection()` et
  `ChangeDetectionStrategy.OnPush` sur tous les composants.
- API à signaux : `signal`, `computed`, `effect`, `linkedSignal`, `input()`,
  `output()`, `model()`, `toSignal()`. Pas de décorateurs `@Input`/`@Output`
  dans le code neuf.
- Angular Material et Bootstrap sont interdits : Tailwind v4 uniquement.

## Organisation des dossiers

```text
src/app/
├── configurations/            # app-settings.ts (nom, logo, langues, thème), api-endpoints.ts, constantes
├── core/
│   ├── auth/                  # AuthService (contrat) + implémentation du fournisseur d'identité
│   ├── guards/                # guards fonctionnels
│   ├── interceptors/          # intercepteurs fonctionnels
│   ├── theme/                 # ThemeService (light / dark / system)
│   └── i18n/                  # configuration de la traduction
├── features/
│   └── {domaine}/
│       ├── components/        # écrans et composants du domaine
│       ├── models/            # basic/, full/, custom/, enums/
│       └── services/          # services héritant de CrudService
├── shared/
│   ├── components/            # UI réutilisable : skeleton, utils-wrapper, notification, popup, page-header, empty-state
│   ├── models/                # api-response.model.ts, crud.model.ts
│   └── services/              # crud.service.ts, utils.service.ts, error-messages.ts
├── app.config.ts              # providers Angular (routeur, HTTP, zoneless, AuthService, i18n)
├── app.routes.ts
├── app.ts / app.html
public/
├── i18n/fr.json, en.json
└── branding/logo.svg, logo-dark.svg
```

- `core/` et `configurations/` ne dépendent jamais de `features/`.
- `shared/` ne contient rien de spécifique à un domaine.
- Un domaine de `features/` n'importe jamais un autre domaine : l'échange passe
  par un service injecté.
- Des sous-dossiers peuvent être créés **dans** ces dossiers ; tout fichier
  hors de cette arborescence est justifié dans la merge request.
- `app.config.ts` est réservé aux providers Angular : le branding et les
  réglages publics sont dans `configurations/app-settings.ts`.

### Fichiers d'un composant

Chaque composant a au minimum `nom.ts` et `nom.html` (`templateUrl`) ;
`template:` et `styles:` en ligne sont interdits. Un `nom.css` n'existe que
pour un besoin technique impossible en classes utilitaires.

## Modèles : Basic, Full, Custom

| Modèle | Miroir backend | Dossier | Usage |
|---|---|---|---|
| `{PROJET}{Entite}BasicModel` | `…BasicDTO` | `models/basic/` | tableaux, listes déroulantes, clés étrangères |
| `{PROJET}{Entite}FullModel` (`extends` Basic) | `…FullDTO` | `models/full/` | détail, formulaires |
| `{PROJET}{Entite}{Usage}CustomModel` | `…CustomDTO` | `models/custom/` | vue d'agrégation |
| enums | enums backend (mêmes valeurs) | `models/enums/` | statuts, types |

Renommer un DTO backend impose de renommer le modèle front dans la même itération.

```typescript
// shared/models/api-response.model.ts — miroir de ApiResponse<T> backend
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  errorCode: string | null;
  data: T;
  timestamp: string;
}

// shared/models/crud.model.ts
export interface CallState {
  loading: boolean;
  errorCode: string | null; // code backend, ex. TNA-MET-001
  errorKey: string | null;  // clé i18n du message à afficher
}
export interface CrudState<B, F extends B> {
  items: B[];
  selected: F | null;
  call: CallState;
}
export const idleCall = (): CallState => ({ loading: false, errorCode: null, errorKey: null });
export const loadingCall = (): CallState => ({ loading: true, errorCode: null, errorKey: null });
```

## Services et état : `CrudService`

Classe abstraite commune : elle déballe l'`ApiResponse`, tient l'état en
signaux et traduit les erreurs en clés i18n.

Contrat public de `CrudService<B, F>` (code complet : `docs/templates/angular/crud.service.ts.example`) :

| Membre | Rôle |
|---|---|
| `items`, `selected`, `isLoading`, `errorKey` | signaux en lecture seule exposés aux composants |
| `getAll()`, `getById(id)`, `create(payload)`, `update(id, payload)`, `delete(id)` | appels HTTP ; mettent à jour l'état |
| `call(request, onSuccess)` (protégé) | passe en chargement, déballe `ApiResponse.data`, applique la mise à jour, gère l'erreur |
| `patch(partial)` (protégé) | modification immuable de l'état |
| `fail(err)` (protégé) | lit `err.error.errorCode`, calcule la clé i18n, relance l'erreur |

Règles : classe **abstraite** sans `@Injectable` ; `HttpClient` par `inject()` ;
endpoint transmis par `super(...)` depuis la sous-classe ; toute méthode
ajoutée par une sous-classe passe par `call()`.

```typescript
@Injectable({ providedIn: 'root' })
export class {PROJET}{Entite}Service extends CrudService<{PROJET}{Entite}BasicModel, {PROJET}{Entite}FullModel> {
  constructor() {
    super(API_ENDPOINTS.{entite}); // configurations/api-endpoints.ts : '/api/v1/{ressource}'
  }
  // Méthodes spécifiques via this.call(...), ex. envoi multipart createWithFile(payload, file)
}
```

Modèle complet, dont l'envoi de fichier multipart :
`docs/templates/angular/entity.service.ts.example`. Type, taille et aperçu
d'un fichier sont des règles propres au projet.

### Messages d'erreur

Le message technique renvoyé par le serveur n'est jamais affiché. Le code
d'erreur backend devient une clé i18n ; les textes sont dans
`public/i18n/fr.json` et `en.json`, section `errors`.

```typescript
// shared/services/error-messages.ts
export function resolveErrorKey(errorCode: string | null, httpStatus?: number): string {
  if (errorCode) return `errors.${errorCode}`;
  if (httpStatus === 0) return 'errors.network';
  return 'errors.unexpected';
}
```

```json
{ "errors": {
    "TNA-MET-001": "Cette action n'est pas autorisée dans l'état actuel de la ressource.",
    "TNA-SEC-001": "Vous n'avez pas les droits nécessaires pour cette action.",
    "network": "Le serveur est injoignable.",
    "unexpected": "Une erreur inattendue est survenue." } }
```

Tout code ajouté au catalogue `docs/ERROR-CODES.md` a sa traduction `fr` et `en`.

## Sécurité : authentification, intercepteur, guards

L'authentification repose sur un JWT, quel que soit le fournisseur d'identité.
Le contrat est une **classe abstraite** (une interface TypeScript ne peut pas
servir de jeton d'injection).

```typescript
// core/auth/auth.service.ts
export abstract class AuthService {
  abstract readonly isAuthenticated: Signal<boolean>;
  abstract getToken(): string | null;
  abstract hasRole(role: string): boolean;
  abstract login(): void;
  abstract logout(): void;
}
```

`app.config.ts` fournit l'implémentation (`{ provide: AuthService, useClass: KeycloakAuthService }`),
`provideZonelessChangeDetection()`, `provideRouter(routes, withComponentInputBinding())`
et `provideHttpClient(withInterceptors([authInterceptor]))`.
Modèles : `app.config.ts.example`, `auth.service.ts.example`.

- `authInterceptor` (fonction `HttpInterceptorFn`) ajoute `Authorization: Bearer <jeton>`
  et appelle `login()` sur une réponse 401 (`auth.interceptor.ts.example`).
- Les guards retournent `true`, `false` ou un `UrlTree` de redirection
  (`admin.guard.ts.example`).
- L'intercepteur et les guards n'importent jamais le SDK du fournisseur
  d'identité : uniquement `AuthService`.
- Les guards sont des fonctions (`CanActivateFn`), jamais des classes.

## Composants

- Dépendances par `inject()` ; aucun constructeur d'injection.
- Tout état local modifiable (filtres, page, sélection, fichier) est un
  `signal()` ; les valeurs dérivées sont des `computed()`.
- Paramètres de route reçus en `input()` grâce à `withComponentInputBinding()`.

### Écran de liste

Choisir explicitement la pagination :
- **côté client** si le jeu de données est borné (moins de 500 éléments) et
  chargé par `getAll()` ;
- **côté serveur** dès que l'endpoint backend est paginé (`PageResponseDto`) :
  page et taille partent en paramètres, seule la page reçue est en mémoire.
Les deux stratégies ne coexistent jamais sur un même écran.

Modèle complet (filtres, pagination côté client en `computed()`) :
`docs/templates/angular/item-list.ts.example` et `item-list.html.example`.

### Formulaire création / modification

- `inject(NonNullableFormBuilder)`, formulaires typés, validation dès la création.
- `FormArray` pour les lignes dynamiques.
- Mode détecté depuis la route :

```typescript
readonly id = input<string>();                       // :id de la route
readonly isEditing = computed(() => !!this.id());

constructor() {
  effect(() => {
    const id = this.id();
    if (id) this.service.getById(+id).subscribe(item => this.form.patchValue(item));
  });
}
```

- Sélection de fichier : `selectedFile = signal<File | null>(null)` ; les
  contrôles de type, de taille et l'aperçu sont définis par le projet.

## Templates : contrôle de flux, chargement, différé

`*ngIf`, `*ngFor` et `*ngSwitch` sont interdits ; tout `@for` déclare `track`.

**Chargement de données** : état lu sur le service, squelette pendant le
chargement, message traduit en cas d'erreur.

```html
@if (service.isLoading()) {
  <app-skeleton variant="list" />
} @else if (service.errorKey()) {
  <app-error-banner [messageKey]="service.errorKey()!" />
} @else {
  @for (item of page(); track item.id) {
    <app-item-card [item]="item" />
  } @empty {
    <p>{{ 'common.empty' | translate }}</p>
  }
}
```

**Chargement différé de composants** : `@defer` sert à charger plus tard le
**code** d'un composant lourd ou secondaire (graphique, éditeur, onglet non
visible). Ses blocs `@loading` et `@error` concernent le téléchargement de ce
code, pas les données.

```html
@defer (on viewport; prefetch on idle) {
  <app-stats-chart [data]="stats()" />
} @placeholder {
  <app-skeleton variant="card" />
} @loading (minimum 200ms) {
  <app-skeleton variant="card" />
} @error {
  <app-error-banner messageKey="errors.moduleLoad" />
}
```

**Squelette** : tout état de chargement affiche `app-skeleton`
(`shared/components/skeleton/`, variantes `list`, `card`, `form`, `detail`),
jamais un indicateur tournant seul sur fond vide.

```typescript
@Component({ selector: 'app-skeleton', templateUrl: './skeleton.html', changeDetection: ChangeDetectionStrategy.OnPush })
export class SkeletonComponent {
  readonly variant = input<'list' | 'card' | 'form' | 'detail'>('list');
}
```

## Retours utilisateur : `UtilsService` et wrapper unique

Un seul `<app-utils-wrapper />`, placé dans `app.html`, affiche chargement
ponctuel, notifications et confirmations pilotés par `UtilsService`
(`shared/services/utils.service.ts`, état en signaux). Les squelettes restent
la règle pour le chargement des données ; le chargement global est réservé aux
actions ponctuelles (soumission, suppression).

- `showLoading(titleKey, messageKey)` / `hideLoading()`
- `showNotification(type, titleKey, messageKey, params?)` / `hideNotification()`
- `showPopup(type, titleKey, messageKey, onConfirm, onCancel?, params?)` / `hidePopup()`

Tous les textes passent en clés i18n.

```typescript
deleteItem(item: {PROJET}{Entite}BasicModel): void {
  this.utils.showPopup('danger', 'common.confirmTitle', 'items.deleteConfirm', () => {
    this.service.delete(item.id!).subscribe({
      next: () => this.utils.showNotification('success', 'common.success', 'items.deleted'),
      error: () => this.utils.showNotification('error', 'common.error', this.service.errorKey() ?? 'errors.unexpected'),
    });
  }, undefined, { name: item.nom });
}
```

Aucun composant ne crée son propre chargement global, sa notification ou sa
fenêtre de confirmation.

## Layouts et routes par rôle

Modèle : `docs/templates/angular/app.routes.ts.example`.

- Chaque persona a son layout (barre latérale, en-tête avec l'utilisateur
  courant, `<router-outlet />`).
- `canActivate` est déclaré **sur le parent** ; un enfant ne porte un guard
  propre que s'il exige des droits plus stricts.
- Les écrans sont chargés à la demande (`loadComponent` / `loadChildren`).

## Nommage

| Élément | Fichier | Symbole |
|---|---|---|
| Modèle | `{entite}-basic-model.ts`, `{entite}-full-model.ts`, `{entite}-{usage}-custom-model.ts` | `{PROJET}{Entite}BasicModel`… |
| Service | `{entite}.service.ts` | `{PROJET}{Entite}Service` |
| Composant | `{entite}-list.ts` + `.html` | classe `{Entite}List`, sélecteur `app-{entite}-list` |
| Guard | `{regle}.guard.ts` | fonction `{regle}Guard` |
| Intercepteur | `{role}.interceptor.ts` | fonction `{role}Interceptor` |

Le préfixe de sélecteur `app-` est commun à tout le projet.

## Styles : Tailwind CSS v4 et design system

- Classes utilitaires dans les templates, construites sur les **tokens** de
  `@theme` (générés depuis `DESIGN.md`) : pas de couleur, d'arrondi ou d'ombre
  codés en dur, le mode sombre fonctionne sans classe `dark:` par composant.
- Un motif répété devient un composant de `shared/components/`.

| Élément | Classes |
|---|---|
| Carte | `bg-surface text-on-surface border border-outline rounded-lg shadow-sm p-6` |
| Bouton primaire | `px-4 py-2 rounded-md font-semibold bg-primary text-on-primary hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed` |
| Badge succès | `bg-success/15 text-success px-2 py-0.5 rounded-sm text-xs font-semibold` |
| Badge avertissement | `bg-warning/15 text-warning px-2 py-0.5 rounded-sm text-xs font-semibold` |
| Badge erreur | `bg-error/15 text-error px-2 py-0.5 rounded-sm text-xs font-semibold` |

Arrondis limités à 8 px (`rounded-sm`, `rounded-md`, `rounded-lg`), voir
`UI-RADIUS-AND-SHADOW-STANDARDS.md`.

## Thème, langues, réglages publics

- `ThemeService` (`core/theme/`) pilote `data-theme="light|dark"` sur `<html>` ;
  choix `light`, `dark` ou `system`, mémorisé si l'application a un espace
  utilisateur.
- Langues `fr` et `en` ; aucun texte visible en dur dans un template ; clés
  nommées par domaine (`items.deleted`, `common.save`).
- Nom, logo, langues, thème par défaut, liens publics : uniquement dans
  `configurations/app-settings.ts`.
- Appels API en chemins relatifs (`/api/v1/…`) ; `proxy.conf.json` en
  développement, voir `CONFIGURATION-STANDARDS.md`.

Modèles : `docs/templates/angular/`.

## Tests

- Services : `HttpTestingController`, cas nominal et cas d'erreur avec `errorCode`.
- Guards et intercepteurs : cas autorisé et refusé, 401 → `login()`.
- Composants : affichage, interactions, états chargement / vide / erreur.
- Commandes : `npm run lint`, `npm run test`, `npm run build`.
