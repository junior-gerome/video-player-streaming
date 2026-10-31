# Standard de configuration — Spring Boot 4.1 / Angular 20

## 1. Spring Boot configuration

### Règle obligatoire

Tout backend Spring Boot doit utiliser :

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
src/main/resources/application-test.yml
src/main/resources/application-prod.yml
```

`application.properties` est interdit pour les nouvelles configurations.

### Pourquoi

- YAML est plus lisible pour les configurations hiérarchiques.
- Les profils sont plus clairs.
- Les configurations complexes Spring Boot, sécurité, datasource, observabilité et CORS sont plus faciles à relire.
- Cela évite la dispersion des formats dans l’équipe.

### Règles

- Ne pas créer `application.properties`.
- Ne pas mélanger YAML et properties dans le même projet.
- Utiliser les variables d’environnement via `${VAR_NAME:default}` ; jamais de valeur par défaut pour un secret (`${DB_PASSWORD}`).
- Activer les threads virtuels Java 21 : `spring.threads.virtual.enabled: true`.
- Modèle : `docs/templates/spring/application.yml.example`.
- Ne jamais mettre de secret réel dans les fichiers versionnés.
- Créer un ADR si un projet existant impose temporairement `application.properties`.
- Prévoir une tâche de migration vers YAML si un fichier `.properties` existe déjà.

## 2. Angular proxy configuration

### Règle obligatoire

Tout frontend Angular doit contenir un proxy de développement :

```text
proxy.conf.json
```

Et `angular.json` doit référencer :

```json
"proxyConfig": "proxy.conf.json"
```

### Règles Angular

- Les services Angular doivent appeler l’API avec des chemins relatifs : `/api/...`, `/auth/...`.
- Ne jamais hardcoder `http://localhost:8080`, une IP ou un domaine backend dans un service Angular.
- Le proxy sert au développement local uniquement.
- En production, utiliser un reverse proxy, gateway, ingress ou configuration serveur.
- Ne pas stocker de secrets dans le proxy.

## 3. Critères de review bloquants

Une review doit être bloquée si :

- un nouveau `application.properties` est introduit ;
- le backend Spring Boot n’a pas de `application.yml` ;
- le frontend Angular n’a pas de `proxy.conf.json` ;
- `angular.json` ne référence pas le proxy ;
- un service Angular hardcode l’URL backend ;
- une exception n’est pas couverte par ADR.

## Configuration Frontend/Mobile

Les projets Angular et Flutter doivent centraliser les informations applicatives et de branding :
Angular dans `src/app/configurations/app-settings.ts` (modèle `docs/templates/angular/app-settings.ts.example`),
Flutter dans `AppConfig`. Le fichier Angular `app.config.ts` reste réservé aux providers.

### Paramètres minimum

| Paramètre | Description |
|---|---|
| `appName` | Nom affiché de l’application |
| `appShortName` | Nom court si nécessaire |
| `appSlogan` | Baseline ou promesse courte |
| `publisherName` | Organisation / éditeur |
| `logoPath` | Logo principal |
| `logoDarkPath` | Logo adapté au thème dark si nécessaire |
| `defaultLocale` | Locale par défaut, recommandée : `fr` |
| `supportedLocales` | Minimum : `fr`, `en` |
| `defaultTheme` | `light`, `dark` ou `system` |
| `supportEmail` | Contact support si applicable |
| `publicLinks` | CGU, confidentialité, contact, site web |

Ces valeurs ne doivent pas être dupliquées dans les écrans.
