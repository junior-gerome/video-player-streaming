# Standard Mobile — Flutter

> Chargé en mode Engineering Flutter, avec `DESIGN.md`. Le pendant web est
> `FRONTEND-ANGULAR-STANDARDS.md` ; les principes communs (backend maître,
> SOLID, limites de taille) sont dans `ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md`.

## 1. Règles non négociables

Toute fonctionnalité Flutter doit respecter :

- `DESIGN.md` comme source de vérité design pour toute UI significative ;
- thème centralisé ;
- deux thèmes minimum : `light` et `dark` ;
- composants/widgets réutilisables ;
- internationalisation minimum : français (`fr`) et anglais (`en`) ;
- configuration applicative centralisée : nom de l’app, logo, slogan, éditeur, paramètres publics, liens, thème par défaut ;
- aucun texte visible codé en dur ;
- aucun branding codé en dur dans un écran.

## 2. Structure et règles Flutter

### 2.1 Structure

```text
lib/
  core/
    config/
      app_config.dart
    theme/
      app_theme.dart
      app_theme_tokens.dart
      theme_controller.dart
    l10n/
      localization_extensions.dart
  shared/
    widgets/
      app_button.dart
      app_text_field.dart
      app_card.dart
      app_empty_state.dart
      app_page_header.dart
  features/
  l10n/
    app_fr.arb
    app_en.arb
l10n.yaml
```

### 2.2 Découpage d'une feature

```text
features/<feature>/
  domain/          # entités, use cases, interfaces de repository
  data/            # DTO, sources de données, implémentations de repository
  presentation/    # écrans, widgets, controllers/providers
```

- Les widgets ne portent pas de logique métier ; les use cases orchestrent.
- Interfaces de repository dans `domain`, implémentations dans `data`.
- Les modèles `data` reprennent le nommage des DTO backend (Basic / Full / Custom).
- Les erreurs backend sont traduites via leur `errorCode` (clé ARB `error_<code>`),
  jamais affichées brutes.
- Données sensibles dans `flutter_secure_storage`.

### 2.3 Thème

- Prévoir `ThemeData` light et dark ou équivalent projet.
- Centraliser les couleurs, typographies, espacements et radius.
- Ne pas mettre des couleurs arbitraires directement dans les widgets métier.
- Le thème doit être appliqué globalement au `MaterialApp` ou équivalent.

### 2.4 Internationalisation

- Minimum obligatoire : `fr` et `en`.
- Prévoir `l10n.yaml` et des fichiers ARB ou une alternative documentée.
- Aucun texte utilisateur ne doit être hardcodé dans les widgets.
- Les formats de date, nombre et devise doivent être compatibles locale.

### 2.5 Configuration applicative

- Centraliser le nom de l’app, les assets de logo, le thème par défaut, les langues supportées et les liens publics dans `AppConfig`.
- Ne pas coder en dur ces valeurs dans les écrans.
- Les valeurs variables par environnement doivent être injectées via `--dart-define`, fichiers de configuration ou mécanisme validé.

## 3. Composants réutilisables

Créer ou réutiliser des composants/widgets pour :

- boutons ;
- champs de saisie ;
- cartes ;
- modales/dialogs ;
- états vide/erreur/chargement ;
- en-têtes de page ;
- badges/statuts ;
- tableaux/listes ;
- navigation ;
- layout de page.

Une interface ne doit pas dupliquer ces éléments dans chaque écran.

## 4. Règle DESIGN.md obligatoire

Avant toute UI Flutter, lire ou créer `DESIGN.md`. Le fichier doit contenir les tokens couleurs, typographie, spacing, radius et composants, ainsi que les sections Overview, Colors, Typography, Layout, Elevation & Depth, Shapes, Components et Do's and Don'ts.

Voir `docs/standards/DESIGN-SYSTEM-STANDARDS.md`.

## 5. Critères de rejet en review

Bloquer la PR/MR si :

| Violation | Décision |
|---|---|
| Pas de thème centralisé pour une nouvelle UI | Rejet |
| Pas de dark/light | Rejet sauf ADR |
| Textes visibles hardcodés | Rejet |
| Pas de traduction `fr` / `en` | Rejet |
| Nom app/logo hardcodés dans un écran | Rejet |
| UI significative sans `DESIGN.md` | Rejet |
| Composant dupliqué au lieu d’un shared component | Correction demandée |
| Couleurs/spacing arbitraires non justifiés | Correction demandée |

## 6. Definition of Done UI

Une tâche UI Flutter est terminée seulement si :

- le thème light est vérifié ;
- le thème dark est vérifié ;
- les textes sont traduits en `fr` et `en` ;
- les composants réutilisables sont utilisés ou créés ;
- la configuration app/branding est centralisée ;
- les états loading/empty/error sont prévus ;
- les tests nécessaires sont listés ou ajoutés ;
- les impacts accessibilité sont vérifiés.

> Arrondis et ombres : `UI-RADIUS-AND-SHADOW-STANDARDS.md`.
