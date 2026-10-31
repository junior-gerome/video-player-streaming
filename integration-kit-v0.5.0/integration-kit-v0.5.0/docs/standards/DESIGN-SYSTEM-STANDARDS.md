# DESIGN SYSTEM STANDARDS — Google DESIGN.md + Tailwind CSS v4 + Flutter

> Objectif : garantir une cohérence visuelle stricte sur le web Angular et le mobile Flutter en donnant aux IA et aux développeurs une source de vérité design lisible par l’humain et exploitable par la machine.

Ce standard s’applique à toute création ou modification d’interface Angular ou Flutter.

## 1. Règle non négociable

Tout projet avec interface utilisateur doit avoir une source de vérité design :

```text
DESIGN.md
```

Avant de créer ou modifier un écran, composant, widget, thème, style, page ou parcours UI, l’IA ou le développeur lit `DESIGN.md` et le standard de la technologie touchée : `FRONTEND-ANGULAR-STANDARDS.md` (web) ou `MOBILE-FLUTTER-STANDARDS.md` (mobile). Ce fichier-ci n’est lu que pour créer ou modifier des tokens.

Si `DESIGN.md` n’existe pas encore et que la demande concerne l’UI, il faut créer une première version depuis :

```text
docs/templates/design/DESIGN.md.example
```

Aucune UI significative ne doit être développée sans tokens de design, règles de composants, stratégie light/dark et règles d’accessibilité documentées.

## 2. Format attendu du `DESIGN.md`

Le format attendu reprend l’approche Google `DESIGN.md` :

- front matter YAML lisible par les agents et les outils ;
- contenu Markdown expliquant le pourquoi des choix visuels ;
- tokens centralisés pour couleurs, typographie, espacements, radius et composants ;
- règles explicites de composants ;
- section Do / Don’t pour éviter les dérives visuelles.

Structure minimale :

```markdown
---
version: alpha
name: Nom du design system
description: Description courte
colors:
  primary: '#2563eb'
  on-primary: '#ffffff'
  secondary: '#64748b'
  on-secondary: '#ffffff'
  tertiary: '#14b8a6'
  on-tertiary: '#042f2e'
  background: '#f8fafc'
  on-background: '#0f172a'
  surface: '#ffffff'
  on-surface: '#0f172a'
  outline: '#cbd5e1'
  error: '#dc2626'
  on-error: '#ffffff'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: 700
    lineHeight: 40px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
rounded:
  sm: 0.25rem
  md: 0.75rem
  lg: 1rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
components:
  button-primary:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    rounded: '{rounded.md}'
    padding: '{spacing.md}'
---

## Overview

## Colors

## Typography

## Layout

## Elevation & Depth

## Shapes

## Components

## Do's and Don'ts
```

## 3. Sections obligatoires

Le `DESIGN.md` doit contenir les sections suivantes, dans cet ordre :

1. `Overview`
2. `Colors`
3. `Typography`
4. `Layout`
5. `Elevation & Depth`
6. `Shapes`
7. `Components`
8. `Do's and Don'ts`

Les sections doivent expliquer les intentions : personnalité visuelle, usage des couleurs, règles typographiques, grilles, espacements, profondeur, radius, composants et interdictions.

## 4. Tokens obligatoires

### 4.1 Couleurs minimales

Définir au minimum :

```text
primary
on-primary
secondary
on-secondary
tertiary
on-tertiary
background
on-background
surface
on-surface
outline
error
on-error
success
on-success
warning
on-warning
```

Recommandé pour les applications sérieuses :

```text
surface-container-lowest
surface-container-low
surface-container
surface-container-high
surface-container-highest
surface-variant
on-surface-variant
primary-container
on-primary-container
secondary-container
on-secondary-container
error-container
on-error-container
```

### 4.2 Typographie minimale

Définir au minimum :

```text
headline-lg
headline-md
body-lg
body-md
body-sm
label-lg
label-md
label-sm
```

Chaque style typographique doit préciser :

- `fontFamily`
- `fontSize`
- `fontWeight`
- `lineHeight`
- `letterSpacing` si utile

### 4.3 Espacement et radius

Les espacements doivent suivre une échelle cohérente, idéalement basée sur `4px` ou `8px`.

Les radius doivent être nommés et réutilisés :

```text
none
sm
md
lg
xl
full
```

## 5. Références de tokens

Les composants doivent référencer les tokens avec la syntaxe :

```text
'{colors.primary}'
'{typography.body-md}'
'{rounded.md}'
'{spacing.md}'
```

Interdictions :

- couleur hexadécimale directement dans un composant si un token existe ;
- spacing arbitraire répété ;
- nom de token non CSS-safe ;
- référence cassée ;
- duplication de valeurs dans Angular et Flutter sans source de vérité.

## 6. Accessibilité et contraste

Toute paire `backgroundColor` / `textColor` d’un composant doit viser au minimum le contraste WCAG AA :

```text
4.5:1 pour le texte normal
```

Une UI doit aussi prévoir :

- focus visible ;
- labels explicites ;
- états erreur/succès/disabled ;
- cibles tactiles suffisantes sur mobile ;
- vérification light et dark ;
- aucun sens transmis uniquement par la couleur.

## 7. Angular — Tailwind CSS v4 uniquement

### 7.1 Règle stricte

Pour Angular, utiliser **Tailwind CSS v4 uniquement**.

Interdictions par défaut :

- Tailwind CSS v3 ;
- configuration v3 basée sur `tailwind.config.js` / `theme.extend` pour les tokens applicatifs ;
- Angular Material ;
- couleurs hardcodées dans les templates ;
- classes arbitraires répétées au lieu d’un composant partagé.

Tailwind v4 doit être utilisé en approche CSS-first :

```css
@import "tailwindcss";

@custom-variant dark (&:where([data-theme="dark"], [data-theme="dark"] *));

@theme {
  --color-primary: #2563eb;
  --color-on-primary: #ffffff;
  --color-surface: #ffffff;
  --color-on-surface: #0f172a;
  --radius-md: 0.375rem; /* 6px, plafond 8px */
  --spacing-md: 16px;
}
```

### 7.2 Export DESIGN.md → Tailwind v4

Quand l’outil `@google/design.md` est disponible, l’export Tailwind v4 attendu est :

```bash
npx @google/design.md lint DESIGN.md
npx @google/design.md export DESIGN.md css-tailwind > src/styles/design-system.theme.css
```

Le fichier généré doit être importé dans le CSS global Angular :

```css
@import "tailwindcss";
@import "./styles/design-system.theme.css";
```

Le format `json-tailwind` / `tailwind` est réservé à l’ancien mode Tailwind v3 et ne doit pas être utilisé pour les nouveaux projets.

### 7.3 Organisation Angular

Arborescence et service de thème : `FRONTEND-ANGULAR-STANDARDS.md`. Le thème
généré est importé dans `src/styles.css` ; le `ThemeService` pilote
`data-theme="light"` / `data-theme="dark"` sur `document.documentElement`.

## 8. Flutter — Design tokens centralisés

Flutter doit réutiliser les mêmes intentions que `DESIGN.md`.

Organisation recommandée :

```text
lib/
  core/
    theme/
      app_design_tokens.dart
      app_theme.dart
      theme_controller.dart
    config/
      app_config.dart
  shared/
    widgets/
```

Règles :

- `ThemeData` light et dark obligatoires ;
- tokens couleurs, typographies, espacements et radius centralisés ;
- pas de `Color(0x...)` arbitraire dans les widgets métier ;
- pas de `TextStyle(...)` dispersé si le style appartient au design system ;
- widgets réutilisables pour boutons, champs, cartes, états, badges, dialogs ;
- l’i18n `fr` / `en` reste obligatoire.

## 9. Workflow obligatoire pour toute UI

Avant le développement :

- [ ] Lire `DESIGN.md` s’il existe.
- [ ] Créer `DESIGN.md` depuis le template s’il n’existe pas et que le projet a une UI.
- [ ] Vérifier les tokens couleurs, typographie, spacing, radius.
- [ ] Vérifier les sections Markdown obligatoires.
- [ ] Vérifier light/dark.
- [ ] Vérifier i18n FR/EN.
- [ ] Identifier les composants réutilisables nécessaires.

Pendant le développement :

- [ ] Utiliser uniquement les tokens.
- [ ] Créer ou réutiliser des composants partagés.
- [ ] Ne pas dupliquer les valeurs visuelles.
- [ ] Maintenir la documentation UI dans `docs/features/<feature-id>/TECHNICAL-DESIGN.md`.

Avant la review :

- [ ] Exécuter ou documenter `npx @google/design.md lint DESIGN.md` si l’outil est disponible.
- [ ] Vérifier l’absence d’Angular Material.
- [ ] Vérifier l’absence de Tailwind v3.
- [ ] Vérifier l’absence de `tailwind.config.js` pour les tokens applicatifs, sauf ADR.
- [ ] Vérifier les contrastes et le focus visible.
- [ ] Vérifier les deux thèmes `light` et `dark`.

## 10. Critères de rejet

Une PR/MR UI doit être refusée si :

| Violation | Décision |
|---|---|
| UI créée sans lecture ou création de `DESIGN.md` | Rejet |
| Tailwind v3 introduit dans Angular | Rejet |
| `tailwind.config.js` utilisé comme source de tokens v3 sans ADR | Rejet |
| Angular Material introduit sans ADR | Rejet |
| Couleurs/typographies/spacings hardcodés alors qu’un token existe | Rejet |
| Texte visible non internationalisé | Rejet |
| Pas de thème dark/light | Rejet |
| Composant dupliqué au lieu d’un composant partagé | Correction obligatoire |
| Contraste insuffisant non justifié | Rejet |

## 11. Definition of Done UI enrichie

Une tâche UI Angular/Flutter est DONE seulement si :

- `DESIGN.md` existe ou a été mis à jour ;
- les tokens nécessaires sont définis ;
- Angular utilise Tailwind CSS v4 CSS-first ;
- Flutter centralise les tokens dans le thème ;
- light et dark sont vérifiés ;
- i18n FR/EN est complète ;
- les composants partagés sont utilisés ;
- les états loading/empty/error/disabled sont prévus ;
- le contraste et l’accessibilité de base sont vérifiés ;
- la documentation fonctionnelle et technique de la feature est à jour.

> Arrondis et ombres : `UI-RADIUS-AND-SHADOW-STANDARDS.md`. Architecture : `ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md`.
