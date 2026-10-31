# Revue — Interface commune (Angular et Flutter)

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.
> Règles : `DESIGN.md`, `docs/standards/DESIGN-SYSTEM-STANDARDS.md`, `UI-RADIUS-AND-SHADOW-STANDARDS.md`.

## Thème, langues, branding

- [ ] `DESIGN.md` existe ou est mis à jour pour toute UI significative.
- [ ] Thème centralisé ; `light` et `dark` implémentés et vérifiés.
- [ ] Couleurs, arrondis et ombres issus des tokens (`@theme` Tailwind v4, `ThemeData` Flutter) ; aucune valeur codée en dur.
- [ ] Langues `fr` et `en` ; aucun texte visible codé en dur.
- [ ] Nom, logo, slogan, éditeur, liens publics centralisés dans la configuration applicative.
- [ ] Aucune syntaxe Tailwind v3 ni `tailwind.config.js` comme source de tokens.

## Arrondis et ombres

- [ ] Cartes, formulaires, champs, boutons et badges : 4 à 6 px, 8 px maximum.
- [ ] Aucun `rounded-full`, `rounded-2xl`, `rounded-3xl` hors avatar ou image circulaire.
- [ ] Ombres légères, cohérentes en light et dark ; exception documentée dans `DESIGN.md` ou un ADR.

## Composants et accessibilité

- [ ] Aucun bloc d'interface dupliqué là où un composant partagé existe.
- [ ] États chargement, vide et erreur traités par des composants réutilisables.
- [ ] Contraste AA, focus visible, libellés explicites, cibles tactiles suffisantes ; aucun sens porté par la seule couleur.

## Fin de tâche UI

- [ ] Light et dark vérifiés ; textes traduits `fr` / `en` ; composants partagés utilisés ou créés.
- [ ] Tests d'affichage et d'interaction listés ou ajoutés ; impacts accessibilité vérifiés.
