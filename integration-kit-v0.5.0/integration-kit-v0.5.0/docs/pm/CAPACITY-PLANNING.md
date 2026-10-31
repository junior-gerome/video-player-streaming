# CAPACITY PLANNING — Calcul de capacité sprint

## 1. Objectif

Déterminer ce que l'équipe peut réellement livrer, au lieu de planifier au feeling.

## 2. Formule

```text
Capacité brute = nombre de jours ouvrés × nombre de personnes
Capacité nette = capacité brute - absences - réunions - support - marge imprévus
Capacité planifiable = capacité nette × taux de focus
```

## 3. Taux de focus recommandé

| Contexte | Taux de focus |
|---|---:|
| Équipe mature, peu d'interruptions | 75-80 % |
| Projet normal | 65-75 % |
| Support client régulier | 60-70 % |
| Projet instable | 50-60 % |
| Équipe très junior | 50-65 % |

## 4. Exemple

Sprint de 10 jours, 3 développeurs :

```text
Capacité brute = 10 × 3 = 30 jours
Réunions/support/absences = 6 jours
Capacité nette = 24 jours
Taux focus = 70 %
Capacité planifiable = 16,8 jours
```

Donc ne pas planifier plus de 16-17 jours de travail.

## 5. Template capacité individuelle

| Développeur | Profil | Jours ouvrés | Absences | Réunions/support | Capacité nette | Taux focus | Capacité planifiable |
|---|---|---:|---:|---:|---:|---:|---:|
| Dev 1 | Senior | 10 | 0 | 2 | 8 | 75 % | 6j |
| Dev 2 | Intermédiaire | 10 | 1 | 2 | 7 | 70 % | 4.9j |
| Dev 3 | Junior | 10 | 0 | 2 | 8 | 60 % | 4.8j |

## 6. Règles

- Garder une marge pour les bugs et urgences.
- Ne pas planifier un junior à 100 %.
- Ne pas mettre le Tech Lead uniquement en production : garder du temps pour review/mentoring.
- Toute tâche non terminée au sprint précédent doit être réévaluée avant report.
- Tout ajout en cours de sprint doit sortir autre chose du sprint ou être marqué comme interruption.

## 7. Signaux d'alerte

| Signal | Action |
|---|---|
| Plus de 80 % de capacité engagée sur projet instable | Réduire le sprint |
| Senior saturé en reviews | Réduire tâches senior ou former intermédiaires |
| Beaucoup de BLOCKED | Revoir dépendances et cadrage |
| Beaucoup de tickets commencés non finis | Limiter WIP |
| Bugs urgents fréquents | Réserver une capacité support |

## Charge documentaire

La capacité sprint doit inclure la documentation.

Règle pratique :

| Type de tâche | Charge documentation recommandée |
|---|---:|
| Bug simple | 5 à 10 % |
| Feature UI/API standard | 10 à 15 % |
| Feature métier complexe | 15 à 25 % |
| Architecture / migration / sécurité | 20 à 30 % |

Cette charge n’est pas du temps perdu : elle réduit les reprises, les oublis, les régressions et la dépendance à un seul développeur.

> Standards techniques non négociables : `AGENTS.md`, section « Standards non négociables » (source unique, ne pas recopier ici).
