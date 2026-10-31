# SCRUM WORKFLOW — Processus adapté

## 1. Cadence recommandée

| Cérémonie | Fréquence | Durée recommandée | Objectif |
|---|---|---:|---|
| Backlog refinement | 1 fois/semaine | 45-60 min | Clarifier et découper |
| Sprint planning | Début sprint | 1-2h | Choisir les tâches selon capacité |
| Daily | Chaque jour | 10-15 min | Voir avancement et blocages |
| Mid-sprint check | Milieu sprint | 30 min | Corriger trajectoire |
| Sprint review | Fin sprint | 45 min | Montrer ce qui est terminé |
| Retrospective | Fin sprint | 30-45 min | Améliorer la méthode |
| Capacity review | Hebdo | 30 min | Ajuster charge et disponibilité |

## 2. Durée de sprint

Recommandation : sprint de 2 semaines.

Utiliser 1 semaine si :

- projet très instable ;
- équipe junior ;
- client change souvent d'avis ;
- besoin de feedback rapide.

Utiliser 3 semaines seulement si :

- équipe mature ;
- peu d'interruptions ;
- livrables lourds.

## 3. États du workflow

```text
BACKLOG → READY → TODO → IN_PROGRESS → REVIEW → QA → DONE
                     ↘ BLOCKED ↗
```

## 4. Règles de passage

### BACKLOG → READY

- critères d'acceptation renseignés ;
- dépendances connues ;
- estimation faite ;
- profil recommandé ;
- données de test disponibles ;
- reviewer identifié.

### TODO → IN_PROGRESS

- assigné à un développeur ;
- capacité disponible ;
- tâche comprise ;
- environnement prêt.

### IN_PROGRESS → REVIEW

- implémentation terminée ;
- tests ajoutés ou justifiés ;
- build local OK ou non exécuté justifié ;
- ticket mis à jour.

### REVIEW → QA

- review technique faite ;
- retours P0/P1 corrigés ;
- risques restants documentés.

### QA → DONE

- critères d'acceptation validés ;
- tests fonctionnels faits ;
- documentation/changelog mis à jour si nécessaire.

## 5. WIP limits

| Profil | WIP recommandé |
|---|---:|
| Junior | 1 tâche en cours |
| Intermédiaire | 1 à 2 tâches |
| Senior | 1 à 3 tâches, selon review/support |
| Tech Lead | 1 tâche focus + reviews |

Trop de tâches en cours donne une illusion d'activité et ralentit la livraison.

## 6. Anti-patterns à éviter

- Démarrer un sprint avec des tickets flous.
- Mettre des tâches de 5 jours sans découpage.
- Ajouter des tâches en cours de sprint sans arbitrage.
- Mesurer uniquement les heures sans regarder la qualité.
- Comparer brutalement les story points entre développeurs.
- Déclarer DONE sans tests ni review.

## Documentation dans Scrum

La documentation démarre au refinement et se poursuit pendant le sprint.

- Product Backlog Refinement : créer le squelette fonctionnel et technique.
- Sprint Planning : vérifier que la story est READY avec documentation initiale.
- Daily : remonter aussi les blocages de documentation ou de règles métier.
- Sprint Review : démontrer la fonctionnalité et confirmer la documentation livrée.
- Retrospective : analyser les écarts dus à une documentation absente, floue ou tardive.

> Standards techniques non négociables : `AGENTS.md`, section « Standards non négociables » (source unique, ne pas recopier ici).
