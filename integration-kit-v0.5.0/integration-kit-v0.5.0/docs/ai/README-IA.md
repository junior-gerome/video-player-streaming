# README IA — Organisation de la documentation

## Où lire

Le point d'entrée unique est `AGENTS.md` (sections « Standards non négociables », « Routage » et « Gabarits »).
Aucun autre fichier n'est lu systématiquement.

## Où écrire

| Information | Fichier |
|---|---|
| Travail d'une intervention | `docs/ai/tickets/TICKET-<id>-<slug>.md` |
| État global (1 ligne par ticket actif) | `docs/ai/PROJECT-TRACKING.md` |
| Tickets DONE d'un sprint clos | `docs/ai/archive/TRACKING-<sprint>.md` |
| Changements notables du projet | `docs/ai/CHANGELOG.md` |
| Décision structurante | `docs/ai/adr/ADR-<id>-<slug>.md` |
| Spécifications d'une feature | `docs/features/<id>/` |
| Sprints, capacité, rapports | `docs/pm/` |
| Versions et releases | `VERSION`, `docs/release/` |

Une information importante ne doit pas rester uniquement dans la conversation.
Ne jamais écrire de secret dans la documentation.

## Hygiène du contexte

- `PROJECT-TRACKING.md` ne contient que les tickets non clos et le sprint
  courant. À la clôture d'un sprint, déplacer les lignes DONE dans
  `docs/ai/archive/`.
- `CHANGELOG.md` : l'IA ne lit que la section `[Unreleased]`.
- Les dossiers `docs/ai/archive/` et `.kit/` ne sont jamais lus sauf demande explicite.
