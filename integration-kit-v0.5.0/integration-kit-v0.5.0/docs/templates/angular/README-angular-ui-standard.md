# Modèles Angular 20

Règles : `docs/standards/FRONTEND-ANGULAR-STANDARDS.md`. Ce dossier ne fournit
que des fichiers de départ.

| Modèle | Destination dans le projet |
|---|---|
| `app-settings.ts.example` | `src/app/configurations/app-settings.ts` (nom, logo, langues, thème par défaut) |
| `theme.service.ts.example` | `src/app/core/theme/theme.service.ts` (light / dark / system) |
| `tailwind-v4-theme.css.example` | `src/styles/design-system.theme.css` (tokens, arrondis plafonnés à 8 px) |
| `styles-theme.css.example` | `src/styles.css` |
| `i18n-fr.json.example`, `i18n-en.json.example` | `public/i18n/fr.json`, `public/i18n/en.json` (dont la section `errors`) |
| `proxy.conf.json.example` + `angular-json-proxy-snippet.md` | `proxy.conf.json` à la racine, référencé dans `angular.json` |
| `app.config.ts.example` | `src/app/app.config.ts` (providers : zoneless, routeur, HTTP, `AuthService`) |
| `app.routes.ts.example` | `src/app/app.routes.ts` (layouts par persona, chargement à la demande) |
| `crud.model.ts.example` | `src/app/shared/models/api-response.model.ts` et `crud.model.ts` (deux fichiers, séparés par un commentaire) |
| `crud.service.ts.example` | `src/app/shared/services/crud.service.ts` |
| `error-messages.ts.example` | `src/app/shared/services/error-messages.ts` |
| `entity.service.ts.example` | `src/app/features/{domaine}/services/{entite}.service.ts` |
| `auth.service.ts.example` | `src/app/core/auth/auth.service.ts` (contrat abstrait) |
| `auth.interceptor.ts.example` | `src/app/core/interceptors/auth.interceptor.ts` |
| `admin.guard.ts.example` | `src/app/core/guards/admin.guard.ts` |
| `item-list.ts.example` + `item-list.html.example` | écran de liste d'un domaine (filtres, pagination côté client) |

Ces modèles compilent ensemble avec Angular 20 en mode strict (`strictTemplates`).

Génération du thème depuis `DESIGN.md` si `@google/design.md` est disponible :

```bat
npx @google/design.md lint DESIGN.md
npx @google/design.md export DESIGN.md css-tailwind > src\styles\design-system.theme.css
```
