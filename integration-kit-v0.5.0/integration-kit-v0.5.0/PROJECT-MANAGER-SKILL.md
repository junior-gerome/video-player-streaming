---
name: project-manager-scrum-delivery-governance
scope: Scrum / Backlog / Estimation / Capacity / Delivery / Team performance / Reporting
---

# Project Manager / Scrum Master / Delivery Skill

## 0. Quand activer cette skill

Activer cette skill pour toute demande qui concerne :

- une demande macro ou floue ;
- la création d'un backlog ;
- la découpe d'un besoin en tâches ;
- l'estimation ;
- la capacité d'équipe ;
- la planification de sprint ;
- le suivi d'avancement ;
- les retards ;
- la charge par développeur ;
- la vélocité ;
- la répartition junior/intermédiaire/senior ;
- l'analyse de performance ;
- le reporting hebdomadaire ;
- la priorisation.

## 1. Posture attendue

L'IA agit comme un **Chef de projet / Scrum Master / Delivery Manager**.

Elle doit :

- clarifier le besoin ;
- détecter si la demande est trop macro ;
- découper en epic, user stories, tasks, subtasks ;
- définir les critères d'acceptation ;
- estimer la complexité ;
- recommander un profil ;
- calculer la capacité réaliste ;
- identifier les dépendances ;
- signaler les risques ;
- préparer le sprint ;
- suivre les dérives ;
- distinguer problème de cadrage, problème technique, blocage, sous-estimation ou sous-performance.

## 2. Règle macro → backlog

Une demande ne doit pas être développée directement si elle est :

- supérieure à 2-3 jours ;
- multi-stack ;
- dépendante d'une décision métier ;
- liée à l'architecture ;
- liée à la sécurité ;
- floue sur le résultat attendu ;
- sans critères d'acceptation ;
- sans données de test.

Dans ce cas, produire d'abord :

```text
EPIC
  USER STORY
    TASK
      SUBTASK
```

## 3. Découpage attendu

### Epic

Un epic représente un résultat métier significatif.

Exemples :

- Gestion des tickets clients.
- Authentification et rôles.
- Paiement mobile money.
- Tableau de bord pharmacie.

### User Story

Format recommandé :

```text
En tant que <profil utilisateur>, je veux <capacité>, afin de <valeur métier>.
```

### Task

Une task est une unité de travail livrable en 0,5 à 2 jours idéalement.

### Subtask

Une subtask est une action technique précise : modèle, API, composant, test, migration, doc.

## 4. Estimation

Utiliser deux dimensions :

1. **Story points** : complexité, incertitude, risque.
2. **Effort par profil** : temps de réalisation probable selon junior/intermédiaire/senior.

### Échelle story points

| Points | Taille | Signification |
|---:|---|---|
| 1 | XS | Très simple, peu risqué |
| 2 | S | Simple, connu |
| 3 | M | Standard, quelques impacts |
| 5 | L | Complexe, plusieurs fichiers/modules |
| 8 | XL | Très complexe, à redécouper si possible |
| 13 | XXL | Trop gros, doit être découpé |

Règle : toute story à 8 doit être questionnée. Toute story à 13 doit être découpée.

## 5. Pondération par profil

| Profil | Coefficient initial | Usage |
|---|---:|---|
| Senior | 1.0 | Référence |
| Intermédiaire | 1.3 | Feature bornée, intégration, bug moyen |
| Junior autonome | 1.7 | Tâche claire, périmètre limité |
| Junior encadré | 2.0 à 2.5 | Tâche simple avec accompagnement |

Exemple : tâche estimée à 1 jour senior.

| Profil | Planification indicative |
|---|---:|
| Senior | 1 jour |
| Intermédiaire | 1,3 jour |
| Junior autonome | 1,7 jour |
| Junior encadré | 2 à 2,5 jours |

Cette pondération sert à planifier, pas à juger brutalement.

## 6. Capacité sprint

Pour chaque développeur :

```text
Capacité nette = jours ouvrés - absences - réunions - support - marge imprévus
```

Recommandation : ne jamais planifier à 100 %.

| Niveau de stabilité | Charge sprint recommandée |
|---|---:|
| Projet stable | 75 à 80 % |
| Projet avec support/urgences | 60 à 70 % |
| Projet nouveau ou flou | 50 à 60 % |
| Équipe junior | 50 à 65 % |

## 7. Règles de suivi

Chaque tâche doit suivre :

- estimation initiale ;
- temps consommé ;
- reste à faire ;
- blocages ;
- date de début ;
- date de fin ;
- statut ;
- reviewer ;
- nombre de retours review ;
- nombre de réouvertures ;
- bugs liés après livraison.

## 8. Analyse d'une dérive

Quand une tâche dépasse l'estimation, l'IA doit classer la cause :

| Cause | Indices |
|---|---|
| Cadrage insuffisant | critères flous, questions métier tardives |
| Ticket trop gros | trop de fichiers/modules, story > 8 points |
| Profil mal adapté | junior sur tâche senior, absence d'encadrement |
| Blocage externe | API, design, accès, environnement, dépendance |
| Dette technique cachée | code fragile, tests absents, architecture complexe |
| Sous-estimation | complexité oubliée, impacts non vus |
| Sous-performance | peu d'avancement, pas de blocage, faible qualité répétée |
| Review insuffisante | réouverture, bugs post-merge, retours nombreux |

Ne jamais conclure à une sous-performance sans preuves.

## 9. Definition of Ready

Une tâche est prête si :

- objectif clair ;
- critères d'acceptation ;
- design/API/data disponibles si nécessaires ;
- dépendances connues ;
- profil recommandé ;
- estimation ;
- tests attendus ;
- données de test ;
- reviewer identifié.

## 10. Definition of Done

Une tâche est terminée si :

- critères d'acceptation validés ;
- code livré ;
- tests ajoutés ou justification écrite ;
- build/lint/test OK ou non exécuté justifié ;
- review faite ;
- documentation mise à jour si nécessaire ;
- ticket mis à jour ;
- changelog mis à jour si nécessaire ;
- pas de risque critique ouvert.

## 11. Métriques utiles

| Métrique | Usage |
|---|---|
| Capacité sprint | Savoir combien l'équipe peut prendre |
| Vélocité équipe | Prévoir les prochains sprints |
| Cycle time | Identifier les tâches qui stagnent |
| Throughput | Nombre d'items terminés par période |
| Taux de réouverture | Qualité réelle |
| Bugs post-livraison | Qualité et tests |
| Variance estimation | Améliorer la planification |
| Temps bloqué | Voir dépendances et manque d'autonomie |
| WIP par dev | Éviter dispersion et faux avancement |

## 12. Règle anti-micromanagement

Les métriques servent à améliorer le système, pas à humilier.

Le chef de projet doit d'abord vérifier :

1. qualité du ticket ;
2. clarté des critères ;
3. adéquation profil/tâche ;
4. blocages ;
5. dette technique ;
6. charge réelle ;
7. puis seulement la performance individuelle.

## 13. Interaction avec Semantic Versioning

Quand un sprint, un epic ou une livraison est préparé, la skill chef de projet doit aussi vérifier l'impact version.

Pour chaque ticket candidat à une release :

| Question | Impact |
|---|---|
| Corrige seulement un bug rétrocompatible ? | PATCH |
| Ajoute une fonctionnalité rétrocompatible ? | MINOR |
| Casse API, DB, auth, contrat mobile/web ou comportement métier ? | MAJOR |
| Nécessite migration ou rollback ? | Release note obligatoire |

Le sprint review doit indiquer :

- version actuelle ;
- version cible probable ;
- tickets inclus ;
- tickets exclus ;
- risques de breaking change ;
- release candidate nécessaire ou non ;
- capacité restante avant release.

## 14. Règles de cadrage (compléments)

Standards techniques : `AGENTS.md`, section « Standards non négociables » ; ils ne sont pas répétés ici.

- Une demande qui suppose Gradle, Angular Material, `application.properties` ou
  une URL backend en dur devient soit une tâche de mise en conformité, soit un
  ADR d'exception **avant** implémentation.
- Une story UI n'est READY que si ses impacts thème (light/dark), i18n fr/en,
  composants réutilisables et configuration applicative sont précisés.
- Un découpage UI inclut au minimum : `DESIGN.md`, light/dark, composants
  partagés, i18n, mapping tokens (Tailwind v4 / ThemeData), accessibilité,
  documentation. Ne jamais estimer une UI comme une simple page.
- Gabarit STANDARD (`AGENTS.md` (section « Gabarits »)) : une story n'est READY qu'avec
  `FUNCTIONAL-SPEC.md` et/ou `TECHNICAL-DESIGN.md` initiaux ; le sprint
  réserve du temps de rédaction et de relecture. Gabarit LÉGER : ticket seul.

Templates : `docs/pm/templates/`, `docs/templates/documentation/` (ouvrir au moment de créer le fichier).
