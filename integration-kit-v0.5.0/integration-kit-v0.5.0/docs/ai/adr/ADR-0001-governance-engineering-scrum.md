# ADR-0001 — Séparer gouvernance technique et gouvernance Scrum

## Statut

Accepté

## Date

2026-07-01

## Contexte

Le kit initial couvrait principalement la qualité technique : lecture du contexte, tickets, tests, sécurité, changelog et suivi. Il ne permettait pas encore de piloter précisément la capacité d'équipe, la découpe macro, l'estimation par profil, la vélocité et les dérives.

## Décision

Séparer la gouvernance en deux couches :

1. `SKILL.md` pour l'ingénierie logicielle ;
2. `PROJECT-MANAGER-SKILL.md` pour le cadrage, Scrum, capacité, estimation et delivery.

## Raisons

- Éviter de mélanger qualité technique et pilotage projet.
- Permettre à l'IA d'activer le bon rôle selon la demande.
- Améliorer la mesure de capacité sans micro-management.
- Découper les besoins macro avant exécution.

## Conséquences positives

- Meilleure visibilité sur la charge.
- Estimation plus réaliste selon junior/intermédiaire/senior.
- Moins de tickets flous.
- Meilleure détection des blocages et dérives.

## Conséquences négatives / risques

- Risque de lourdeur documentaire si appliqué sans discernement.
- Besoin de discipline pour tenir les fichiers à jour.

## Mitigation

- Utiliser un niveau léger pour les tâches simples.
- Activer la couche PM surtout pour macro, sprint, capacité, retard et reporting.

## Références

- Scrum Guide : https://scrumguides.org/
- Evidence-Based Management : https://www.scrum.org/resources/evidence-based-management-guide
