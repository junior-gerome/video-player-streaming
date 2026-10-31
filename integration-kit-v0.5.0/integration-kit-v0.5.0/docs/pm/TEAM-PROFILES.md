# TEAM PROFILES — Profils, autonomie et tâches adaptées

## 1. Profils

### Junior

Un junior peut réaliser des tâches simples et bien cadrées.

Adapté pour :

- correction UI simple ;
- formulaire simple ;
- DTO ;
- mapping simple ;
- test unitaire guidé ;
- bug reproductible avec cause claire ;
- composant isolé ;
- documentation.

À éviter sans accompagnement :

- architecture ;
- sécurité ;
- migration DB critique ;
- refactoring large ;
- bug production complexe ;
- intégration multi-système.

### Intermédiaire

Adapté pour :

- feature bornée ;
- API + UI simple ;
- intégration service ;
- écrans complets ;
- tests unitaires/intégration ;
- bug moyen ;
- refactoring local.

### Senior

Adapté pour :

- architecture ;
- sécurité ;
- bug complexe ;
- refactoring critique ;
- revue de code ;
- mentoring ;
- conception API/data ;
- arbitrage technique ;
- dette technique profonde.

### Tech Lead

Adapté pour :

- arbitrage architecture ;
- standards ;
- revue finale ;
- découpage technique ;
- support seniors/intermédiaires ;
- contrôle qualité ;
- décisions structurantes.

## 2. Matrice d'assignation

| Type de tâche | Junior | Intermédiaire | Senior | Tech Lead |
|---|---|---|---|---|
| UI simple | ✅ | ✅ | ➖ | ➖ |
| Formulaire + validation | ✅ encadré | ✅ | ✅ | ➖ |
| API CRUD simple | ✅ encadré | ✅ | ✅ | ➖ |
| Auth/security | ❌ | ⚠️ | ✅ | ✅ |
| Migration DB | ❌ | ⚠️ | ✅ | ✅ |
| Refactoring large | ❌ | ⚠️ | ✅ | ✅ |
| Bug prod critique | ❌ | ⚠️ | ✅ | ✅ |
| Tests unitaires | ✅ | ✅ | ✅ | ➖ |
| Architecture | ❌ | ❌ | ✅ | ✅ |
| Review PR | ❌ | ⚠️ | ✅ | ✅ |

Légende : ✅ adapté, ⚠️ possible avec encadrement, ❌ éviter, ➖ non prioritaire.

## 3. Critères d'autonomie

| Niveau | Signes observables |
|---|---|
| Faible | demande souvent quoi faire, bloque sans alerter, peu de tests |
| Moyenne | avance sur tâches cadrées, remonte les blocages, corrige review |
| Forte | découpe, anticipe risques, propose tests, documente impacts |
| Lead | améliore le système, encadre, détecte dette, arbitre |

## 4. Règle d'encadrement

Pour chaque junior :

- ticket court ;
- critères d'acceptation explicites ;
- exemple ou référence existante ;
- reviewer senior identifié ;
- checkpoint quotidien ;
- pas plus d'une tâche en cours.

## 5. Évaluation objective

Ne pas conclure qu'un développeur ne travaille pas sans regarder :

- clarté du ticket ;
- complexité réelle ;
- profil adapté ;
- blocages ;
- disponibilité ;
- qualité des reviews ;
- bugs réouverts ;
- temps bloqué ;
- preuve d'activité.

> Standards techniques non négociables : `AGENTS.md`, section « Standards non négociables » (source unique, ne pas recopier ici).
