# QA REVIEW WORKFLOW

## 1. Objectif

Vérifier qu'une tâche est réellement prête à merger ou livrer, en ne lisant que
ce que le diff touche.

## 2. Étapes

1. Lancer `scripts\check-standards.bat` (contrôles automatiques S1–S6, S8, S10).
2. Lire le ticket et ses critères d'acceptation.
3. Lire le diff (`git diff <base>...HEAD`), pas les fichiers entiers.
4. Appliquer `docs/ai/review/BLOQUANTS.md`.
5. Charger uniquement les fiches `docs/ai/review/*.md` touchées par le diff.
6. Vérifier que les tests couvrent les critères d'acceptation.
7. Vérifier doc et changelog selon le gabarit (LÉGER / STANDARD, `AGENTS.md` (section « Gabarits »)).
8. Classer les remarques P0 à P3.

## 3. Sévérité

| Sévérité | Signification | Action |
|---|---|---|
| P0 | Bloquant (critère `BLOQUANTS.md`, sécurité, régression) | Rejet |
| P1 | Problème majeur | Corriger avant merge |
| P2 | Problème mineur | Corriger si simple ou créer un ticket |
| P3 | Nit / style | Optionnel |

## 4. Rapport attendu

Ne lister que les sections non vides.

```markdown
**Verdict** : APPROVED / CHANGES_REQUESTED / BLOCKED
**check-standards** : OK / KO (détails)
**P0 / P1** : fichier:ligne — problème — correction attendue
**P2 / P3** : idem, en bref
**Tests vérifiés** : …
**Risques restants** : …
```
