# Gitignore Standards — Projet Spring Boot / Angular / Flutter

## Objectif

Chaque projet doit avoir un `.gitignore` à la racine, adapté à sa stack réelle, afin d’éviter :

- les secrets dans Git ;
- les fichiers générés inutiles ;
- les dépendances locales ;
- les artefacts de build ;
- les caches d’IDE ;
- les fichiers temporaires IA ;
- les fichiers propres à une machine.

## Règle obligatoire

Un projet doit toujours contenir un fichier `.gitignore` à la racine du dépôt.

Ce fichier doit être adapté au projet :

- Spring Boot backend : Maven, `target/`, logs, secrets, fichiers locaux ;
- Angular frontend : `node_modules/`, `dist/`, `.angular/`, caches, coverage ;
- Flutter mobile : `.dart_tool/`, `build/`, fichiers générés plateformes ;
- monorepo : combinaison contrôlée des règles backend/frontend/mobile ;
- projet avec IA : caches et scratchpads locaux des agents IA.

## Secrets interdits

Ne jamais versionner :

```text
.env
.env.*
application-local.yml
application-secret.yml
application-secrets.yml
*.pem
*.key
*.p12
*.jks
secrets/
credentials/
```

Exception : `.env.example` doit être versionné si le projet utilise des variables d’environnement.

## Spring Boot / Maven

Le backend utilise Maven. Le `.gitignore` doit ignorer :

```text
target/
*.class
hs_err_pid*
replay_pid*
```

Le Maven Wrapper doit généralement rester versionné :

```text
mvnw
mvnw.cmd
.mvn/wrapper/maven-wrapper.properties
```

Le fichier `application.yml` est versionné s’il ne contient pas de secret.
Les fichiers locaux ou sensibles ne doivent pas être versionnés :

```text
application-local.yml
application-secret.yml
application-secrets.yml
```

## Angular / Tailwind CSS v4

Le frontend Angular doit ignorer :

```text
node_modules/
dist/
.angular/
.cache/
coverage/
*.tsbuildinfo
```

Le fichier `proxy.conf.json` doit normalement être versionné car le proxy est obligatoire pour le développement local.

Les variantes locales doivent être ignorées :

```text
proxy.local.conf.json
proxy.*.local.json
```

## Flutter

Le mobile Flutter doit ignorer :

```text
.dart_tool/
.flutter-plugins
.flutter-plugins-dependencies
build/
.pub-cache/
```

Pour les applications Flutter, `pubspec.lock` doit généralement être versionné.
Pour les packages/librairies Flutter, l’équipe peut décider autrement via ADR.

## IA / Agents

Les caches et brouillons locaux d’IA ne doivent pas être versionnés :

```text
.ai-cache/
.ai-tmp/
.codex/
.claude/cache/
.gemini/cache/
*.scratch.md
```

Les fichiers de gouvernance doivent être versionnés :

```text
AGENTS.md
SKILL.md
PROJECT-MANAGER-SKILL.md
CLAUDE.md
GEMINI.md
docs/ai/
docs/pm/
docs/qa/
docs/standards/
docs/templates/
docs/release/
scripts/
.standards-exceptions
```

## Critères de review

Une PR/MR doit être bloquée si :

- un secret est versionné ;
- `target/`, `node_modules/`, `dist/`, `build/` ou `.dart_tool/` sont versionnés ;
- le projet n’a pas de `.gitignore` à la racine ;
- le `.gitignore` ne correspond pas à la stack du projet ;
- le proxy Angular obligatoire est ignoré alors qu’il doit être versionné ;
- un fichier local sensible est ajouté au dépôt ;
- un fichier de gouvernance IA est ignoré par erreur.

## Templates disponibles

```text
docs/templates/gitignore/.gitignore.spring-angular-flutter.example
docs/templates/gitignore/.gitignore.spring-maven.example
docs/templates/gitignore/.gitignore.angular-tailwind-v4.example
docs/templates/gitignore/.gitignore.flutter.example
```

## Règle d’adaptation

L’agent IA doit adapter le `.gitignore` au projet réel.

Il ne doit pas copier un template aveuglément si le projet ne contient pas la stack correspondante.
