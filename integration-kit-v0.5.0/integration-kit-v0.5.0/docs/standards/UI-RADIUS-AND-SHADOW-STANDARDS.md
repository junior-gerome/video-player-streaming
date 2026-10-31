# UI Radius & Shadow Standards — Web / Mobile

## Objectif

Garantir une interface sobre, professionnelle et crédible, adaptée aux produits SaaS, santé, institutionnels et business.

Les interfaces ne doivent pas donner une impression trop “iPhone”, trop “jouet”, trop “cartoon” ou trop arrondie.

## Règle obligatoire

Pour le web Angular et le mobile Flutter :

- utiliser des coins carrés ou très légèrement arrondis ;
- privilégier la profondeur par des ombres légères plutôt que par de grands arrondis ;
- éviter les cards, formulaires, boutons et badges en forme de pilule sauf justification claire ;
- documenter toute exception dans `DESIGN.md`.

## Valeurs recommandées

| Élément UI | Rayon recommandé | Maximum sans ADR |
|---|---:|---:|
| Card / Panel / Modal | 4px à 6px | 8px |
| Bouton principal | 4px à 6px | 8px |
| Input / Select / Textarea | 4px | 6px |
| Badge / Tag | 4px | 8px |
| Avatar / image circulaire | Cercle autorisé | N/A |
| Logo container | 4px à 8px | 8px |
| Bottom sheet mobile | 8px à 12px côté haut uniquement | 16px |
| Toast / Alert | 4px à 6px | 8px |

## Interdictions par défaut

Sont interdits sans justification dans `DESIGN.md` ou ADR :

- `rounded-full` pour les boutons, cards, inputs, badges standards ;
- cards très arrondies type `rounded-2xl`, `rounded-3xl` ou plus ;
- formulaires avec coins trop ronds ;
- design “capsule” généralisé ;
- arrondis incohérents d’un composant à l’autre ;
- ombres lourdes qui donnent un rendu flottant excessif.

## Tailwind CSS v4 — règle attendue

Dans les projets Angular, les tokens de radius doivent être définis en Tailwind CSS v4 via `@theme`.

Exemple :

```css
@import "tailwindcss";

@theme {
  --radius-brand-xs: 0.125rem; /* 2px */
  --radius-brand-sm: 0.25rem;  /* 4px */
  --radius-brand-md: 0.375rem; /* 6px */
  --shadow-brand-card: 0 10px 26px rgb(10 29 61 / 0.10),
                       0 2px 8px rgb(10 29 61 / 0.08);
}
```

Les composants doivent ensuite utiliser ces tokens plutôt que des valeurs arbitraires partout.

## Flutter — règle attendue

Dans les projets Flutter, les rayons doivent être centralisés dans le thème ou dans des tokens partagés.

Exemple :

```dart
abstract final class AppRadius {
  static const double xs = 2;
  static const double sm = 4;
  static const double md = 6;
  static const double lg = 8;
}
```

Les composants doivent utiliser ces constantes et non des valeurs dispersées.

## Ombres recommandées

Les ombres doivent rester sobres :

- légère séparation de surface ;
- pas d’effet flottant exagéré ;
- pas d’ombre noire très opaque ;
- cohérence entre light et dark mode.

Exemple Tailwind v4 :

```css
@theme {
  --shadow-brand-sm: 0 1px 3px rgb(10 29 61 / 0.08);
  --shadow-brand-md: 0 8px 20px rgb(10 29 61 / 0.10);
  --shadow-brand-card: 0 10px 26px rgb(10 29 61 / 0.10),
                       0 2px 8px rgb(10 29 61 / 0.08);
}
```

## Règle DESIGN.md

Chaque `DESIGN.md` doit préciser :

- la stratégie d’arrondis ;
- les tokens de radius utilisés ;
- les cas où un composant peut être circulaire ;
- les règles d’ombres ;
- les exceptions justifiées.

## Critère de review

Une PR/MR doit être bloquée si :

- un composant introduit des arrondis excessifs sans justification ;
- `rounded-full`, `rounded-2xl`, `rounded-3xl` ou équivalent est utilisé hors cas autorisé ;
- les rayons sont définis en dur partout au lieu d’être centralisés ;
- la règle n’est pas documentée dans `DESIGN.md` pour une nouvelle interface.
