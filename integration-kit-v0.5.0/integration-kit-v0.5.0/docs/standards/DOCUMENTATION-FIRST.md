# Documentation First — Standard obligatoire

> La documentation fonctionnelle et technique commence au démarrage du développement, jamais à la fin.

## 1. Objectif

Ce standard impose une approche **documentation continue** pour éviter :

- les fonctionnalités développées sans compréhension métier partagée ;
- les tickets terminés mais non maintenables ;
- les régressions causées par une connaissance restée dans la tête d’un développeur ;
- les livraisons impossibles à reprendre par un autre membre de l’équipe ;
- les écarts entre besoin, implémentation, tests et livraison.

## 2. Règle non négociable

> **Proportionnalité (v0.4)** : cette règle s'applique au gabarit STANDARD.
> Le gabarit LÉGER (`AGENTS.md` (section « Gabarits »)) se limite au ticket, sauf si une
> documentation existante devient fausse. Chaque spécification tient en une page
> au démarrage et s'enrichit ensuite.

Aucun développement significatif ne doit commencer sans documentation initiale.

Avant de coder, l’IA ou le développeur doit créer ou mettre à jour :

```text
docs/features/<feature-id>/FUNCTIONAL-SPEC.md
docs/features/<feature-id>/TECHNICAL-DESIGN.md
```

Selon le besoin, ajouter aussi :

```text
docs/features/<feature-id>/API-CONTRACT.md
docs/features/<feature-id>/DATA-MODEL.md
docs/features/<feature-id>/TEST-PLAN.md
docs/features/<feature-id>/USER-GUIDE.md
```

## 3. Documentation minimale au démarrage

Au démarrage, la documentation peut être courte, mais elle doit exister.

### Documentation fonctionnelle minimale

- Problème métier à résoudre
- Utilisateurs concernés
- Objectif de la fonctionnalité
- Périmètre inclus / exclu
- Parcours utilisateur attendu
- Règles métier connues
- Critères d’acceptation
- Cas limites connus
- Hypothèses et zones à clarifier

### Documentation technique minimale

- Stack concernée : Spring Boot, Angular, Flutter, DB, CI/CD
- Modules/fichiers probablement impactés
- Architecture cible ou approche technique
- Contrats API attendus si applicable
- Modèle de données / migrations si applicable
- Configuration nécessaire
- Sécurité et permissions
- Logs / observabilité attendus
- Stratégie de tests
- Impact SemVer prévu : aucun / PATCH / MINOR / MAJOR

## 4. Documentation continue pendant le développement

La documentation doit évoluer à chaque changement important :

| Événement | Documentation à mettre à jour |
|---|---|
| Nouvelle règle métier découverte | `FUNCTIONAL-SPEC.md` |
| Nouveau endpoint / payload / erreur API | `API-CONTRACT.md` ou OpenAPI |
| Nouvelle table, colonne, migration | `DATA-MODEL.md` + migration docs |
| Changement d’architecture | `TECHNICAL-DESIGN.md` + ADR si structurant |
| Changement UI / parcours | `FUNCTIONAL-SPEC.md` + captures si disponibles |
| Changement thème / i18n / branding | docs front/mobile + traductions |
| Bug découvert | ticket + section cas limites / tests |
| Décision de version | release note + décision SemVer |

## 5. Definition of Ready documentaire

Une story ou tâche n’est pas READY si :

- [ ] la documentation fonctionnelle initiale n’existe pas ;
- [ ] la documentation technique initiale n’existe pas pour une tâche technique ;
- [ ] les critères d’acceptation ne sont pas écrits ;
- [ ] les hypothèses ne sont pas explicites ;
- [ ] les impacts API, DB, UI, sécurité, configuration ou version ne sont pas évalués ;
- [ ] les tests attendus ne sont pas décrits.

## 6. Definition of Done documentaire

Une story ou tâche n’est pas DONE si :

- [ ] la documentation fonctionnelle n’a pas été mise à jour ;
- [ ] la documentation technique n’a pas été mise à jour ;
- [ ] les endpoints, DTO, erreurs ou règles API ne sont pas documentés ;
- [ ] les changements DB ou configuration ne sont pas documentés ;
- [ ] les textes i18n FR/EN ne sont pas documentés si UI/mobile ;
- [ ] le changelog n’est pas mis à jour si le comportement change ;
- [ ] la décision SemVer n’est pas indiquée si le changement est livrable ;
- [ ] les tests réalisés ou non réalisés ne sont pas tracés.

## 7. Règle IA

Quand une IA reçoit une demande de développement, elle doit :

1. créer ou mettre à jour le ticket ;
2. créer ou mettre à jour la documentation fonctionnelle ;
3. créer ou mettre à jour la documentation technique ;
4. seulement ensuite proposer ou modifier le code ;
5. maintenir la documentation en parallèle du code ;
6. résumer à la fin les documents créés ou modifiés.

## 8. Règle Chef de projet / Scrum

Pendant le refinement ou le sprint planning :

- une epic doit avoir une vision fonctionnelle documentée ;
- une user story doit avoir des critères d’acceptation documentés ;
- une tâche technique doit avoir une note technique minimale ;
- une tâche de plus de 2-3 jours doit être découpée et documentée par sous-tâche ;
- le reviewer doit vérifier la documentation autant que le code.

## 9. Emplacement recommandé

```text
docs/features/
  <feature-id>/
    FUNCTIONAL-SPEC.md
    TECHNICAL-DESIGN.md
    API-CONTRACT.md
    DATA-MODEL.md
    TEST-PLAN.md
    USER-GUIDE.md
```

Exemple :

```text
docs/features/ticket-management/
  FUNCTIONAL-SPEC.md
  TECHNICAL-DESIGN.md
  API-CONTRACT.md
  TEST-PLAN.md
```

## 10. Critère bloquant en review

Une PR/MR doit être bloquée si :

- elle ajoute ou modifie un comportement sans documentation ;
- elle modifie une API sans contrat documenté ;
- elle modifie la base sans migration ou note de données ;
- elle ajoute de l’UI sans textes FR/EN prévus ;
- elle ajoute de la configuration sans explication dans `application.yml`, `app.config.ts`, `app_config.dart` ou README ;
- elle livre un changement sans changelog ou décision SemVer lorsque requis.
