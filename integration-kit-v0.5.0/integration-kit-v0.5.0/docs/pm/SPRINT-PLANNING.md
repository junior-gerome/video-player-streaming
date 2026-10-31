# SPRINT PLANNING — Préparer un sprint

## 1. Entrées nécessaires

- Objectif du sprint.
- Capacité réelle de l'équipe.
- Backlog priorisé.
- Tickets READY.
- Disponibilités.
- Dépendances.
- Risques connus.

## 2. Étapes

1. Définir l'objectif du sprint.
2. Calculer la capacité planifiable.
3. Sélectionner uniquement des tickets READY.
4. Vérifier l'équilibre junior/intermédiaire/senior.
5. Garder une marge pour bugs/support.
6. Identifier les reviewers.
7. Valider le DoR et le DoD.
8. Documenter dans `docs/pm/sprints/SPRINT-XXXX.md`.

## 3. Règles

- Ne pas planifier de ticket flou.
- Ne pas charger à 100 %.
- Ne pas donner plusieurs gros tickets à un même développeur.
- Ne pas démarrer une tâche bloquée par une décision métier.
- Ne pas planifier une story à 13 points : découper.

## 4. Sortie attendue

Le sprint plan doit contenir :

- objectif ;
- capacité ;
- tickets sélectionnés ;
- charge totale ;
- charge par développeur ;
- risques ;
- dépendances ;
- marge ;
- définition de succès.

## Capacité documentation

Lors du sprint planning, prévoir explicitement du temps pour :

- créer la documentation fonctionnelle ;
- créer la documentation technique ;
- documenter API / DB / configuration si impact ;
- mettre à jour changelog et décision SemVer ;
- relire la documentation en review.

Une story sans documentation initiale ne doit pas entrer en sprint, sauf urgence explicitement marquée et régularisée avant `DONE`.

> Standards techniques non négociables : `AGENTS.md`, section « Standards non négociables » (source unique, ne pas recopier ici).
