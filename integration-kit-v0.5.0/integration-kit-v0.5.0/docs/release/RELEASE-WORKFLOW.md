# RELEASE WORKFLOW — Préparer et publier une version

## 1. Objectif

Garantir qu'une version livrée est traçable, testée, documentée, taguée et rollbackable.

## 2. Déclencheurs

Activer ce workflow quand la demande contient :

- release ;
- version ;
- tag ;
- changelog ;
- livraison ;
- déploiement ;
- rollback ;
- hotfix ;
- release candidate ;
- semantic versioning ;
- SemVer.

## 3. Entrées obligatoires

- Liste des tickets inclus.
- Version actuelle.
- Changements API / DB / UI / mobile.
- Résultats de tests.
- Statut QA.
- Risques et rollback.

## 4. Étapes de release

1. Lire `docs/release/SEMANTIC-VERSIONING.md`.
2. Lire `docs/ai/CHANGELOG.md`.
3. Lire les tickets concernés.
4. Identifier les changements PATCH / MINOR / MAJOR.
5. Déterminer la prochaine version.
6. Mettre à jour `VERSION`.
7. Mettre à jour `docs/release/VERSION-MATRIX.md` si monorepo ou multi-app.
8. Mettre à jour `docs/ai/CHANGELOG.md`.
9. Vérifier les tests et la QA.
10. Préparer la note de release.
11. Proposer le tag Git.
12. Documenter le rollback.

## 5. Release candidate

Avant une version stable, utiliser :

```text
x.y.z-rc.1
x.y.z-rc.2
```

Règles :

- `rc.1` est créée après gel fonctionnel.
- Toute correction après RC crée une nouvelle RC.
- La version stable reprend le même `x.y.z` sans suffixe si aucun changement majeur n'est ajouté.

## 6. Hotfix

Un hotfix corrige un problème critique en production.

Règles :

- partir du tag de production ;
- corriger uniquement le bug critique ;
- bump PATCH sauf breaking change exceptionnel ;
- mettre à jour changelog ;
- taguer ;
- reporter ensuite la correction sur la branche principale.

## 7. Release checklist

- [ ] Tickets inclus listés
- [ ] Version actuelle identifiée
- [ ] Bump SemVer justifié
- [ ] Breaking changes identifiés
- [ ] Migrations DB vérifiées
- [ ] API contract vérifié
- [ ] Angular build/test OK
- [ ] Maven uniquement pour backend Spring Boot vérifié
- [ ] Tailwind CSS v4 utilisé et Angular Material absent pour Angular
- [ ] Spring Boot configuré via `application.yml` / profils YAML, sans nouveau `application.properties`
- [ ] Angular proxy vérifié : `proxy.conf.json`, `proxyConfig` dans `angular.json`, appels API relatifs
- [ ] Flutter analyze/test OK
- [ ] Backend tests OK
- [ ] Security checks OK
- [ ] Changelog mis à jour
- [ ] `VERSION` mis à jour
- [ ] `VERSION-MATRIX.md` mis à jour si applicable
- [ ] Note de release prête
- [ ] Rollback documenté
- [ ] Tag Git proposé

## 8. Réponse attendue de l'IA

```markdown
## Release proposée

## Version actuelle

## Version cible

## Justification SemVer

## Tickets inclus

## Changements par catégorie

## Breaking changes

## Tests / QA

## Changelog

## Tag Git proposé

## Rollback

## Risques restants
```

## Contrôles complémentaires

- [ ] `scripts\check-standards.bat` vert (standards non négociables d'`AGENTS.md`)
- [ ] Documentation feature, API et data à jour pour les tickets inclus
- [ ] UI : light/dark, i18n fr/en, `DESIGN.md` à jour (si applicable)
