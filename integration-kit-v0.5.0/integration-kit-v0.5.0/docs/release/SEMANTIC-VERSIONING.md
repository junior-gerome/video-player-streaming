# SEMANTIC VERSIONING — Règles de version du projet

> Objectif : versionner proprement les livraisons Spring Boot / Angular / Flutter, éviter les changements silencieux et rendre les impacts visibles.

## 1. Format obligatoire

Toute version applicative doit respecter :

```text
MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
```

Exemples :

```text
1.4.2
1.5.0-rc.1
2.0.0-beta.3
1.4.2+build.20260701
```

## 2. Règle de bump

| Type de changement | Bump | Exemple |
|---|---:|---|
| Correction de bug rétrocompatible | PATCH | `1.2.3` → `1.2.4` |
| Nouvelle fonctionnalité rétrocompatible | MINOR | `1.2.3` → `1.3.0` |
| Changement cassant API, DB, auth, contrat mobile/web | MAJOR | `1.2.3` → `2.0.0` |
| Version de test interne | prerelease | `1.3.0-alpha.1` |
| Release candidate | prerelease | `1.3.0-rc.1` |

## 3. Changements PATCH

Utiliser PATCH pour :

- bug fix sans changement de contrat ;
- correction UI sans impact fonctionnel majeur ;
- correction de test ;
- amélioration de log non cassante ;
- optimisation interne sans changement visible ;
- correction de configuration rétrocompatible.

Exemples :

```text
fix(api): correct null handling in ticket search
fix(ui): correct button alignment on mobile
fix(flutter): prevent crash when token is expired
```

## 4. Changements MINOR

Utiliser MINOR pour :

- nouvelle fonctionnalité rétrocompatible ;
- nouveau endpoint sans casser l'ancien ;
- nouvel écran ;
- nouveau champ optionnel ;
- nouveau workflow compatible avec l'existant ;
- amélioration observable sans rupture.

Exemples :

```text
feat(ticket): add customer ticket history
feat(api): add optional priority filter
feat(flutter): add offline draft mode
```

## 5. Changements MAJOR

Utiliser MAJOR pour tout changement cassant, notamment :

- suppression ou renommage d'endpoint ;
- suppression ou renommage de champ API ;
- changement de type de donnée exposé ;
- changement de comportement métier non rétrocompatible ;
- migration DB destructive ;
- modification du modèle d'autorisation ;
- changement obligatoire côté Angular ou Flutter ;
- changement de format de token, session ou auth ;
- suppression d'une configuration publique.

Exemple :

```text
feat(api)!: replace ticket status model
```

ou :

```text
BREAKING CHANGE: `status` is replaced by `workflowState` in all ticket APIs.
```

## 6. Règles strictes pour l'IA

Avant de proposer une version, l'IA doit analyser :

- les tickets inclus ;
- les changements API ;
- les migrations DB ;
- les impacts Angular ;
- les impacts Flutter ;
- les impacts sécurité ;
- les impacts configuration ;
- les changements de comportement utilisateur ;
- les risques de rollback.

L'IA ne doit jamais annoncer une version sans expliquer pourquoi le bump est PATCH, MINOR ou MAJOR.

## 7. Mapping Conventional Commits → SemVer

| Commit | Bump par défaut |
|---|---:|
| `fix:` | PATCH |
| `perf:` rétrocompatible | PATCH |
| `feat:` | MINOR |
| `refactor:` sans rupture | PATCH |
| `docs:` seul | aucun bump applicatif ou PATCH documentaire |
| `test:` seul | aucun bump applicatif ou PATCH interne |
| `chore:` seul | aucun bump applicatif sauf packaging |
| `security:` correction rétrocompatible | PATCH |
| `feat!:` / `fix!:` / `BREAKING CHANGE:` | MAJOR |

## 8. Versions multi-applications

Si le dépôt contient plusieurs applications, maintenir une matrice :

```text
docs/release/VERSION-MATRIX.md
```

Exemple :

| Module | Version | Type | Commentaire |
|---|---:|---|---|
| Backend Spring Boot | 1.4.0 | MINOR | Nouveaux endpoints tickets |
| Angular Web | 1.4.0 | MINOR | UI tickets |
| Flutter Mobile | 1.3.2 | PATCH | Correction affichage |
| API Contract | 1.4.0 | MINOR | Champ optionnel ajouté |

## 9. Git tags

Chaque release validée doit avoir un tag Git :

```bash
git tag -a v1.4.0 -m "Release v1.4.0"
git push origin v1.4.0
```

Pour un monorepo multi-apps, utiliser au besoin :

```bash
git tag -a backend-v1.4.0 -m "Backend release v1.4.0"
git tag -a web-v1.4.0 -m "Web release v1.4.0"
git tag -a mobile-v1.3.2 -m "Mobile release v1.3.2"
```

## 10. Changelog obligatoire

Toute release doit mettre à jour :

```text
docs/ai/CHANGELOG.md
```

La section `[Unreleased]` doit être vidée ou reportée dans la version publiée.

## 11. Décision de version — format obligatoire

À chaque préparation de release, l'IA doit produire :

```markdown
## Décision de version

Version actuelle : x.y.z
Version proposée : x.y.z
Bump : PATCH / MINOR / MAJOR

### Justification
- ...

### Changements inclus
- ...

### Breaking changes
- Aucun / Liste

### Migrations
- Aucune / Liste

### Rollback
- Procédure courte
```

## Impact SemVer des changements UI/branding/i18n

- Correction de traduction, thème ou logo sans changement fonctionnel : généralement `PATCH`.
- Ajout d’un nouveau thème, nouvelle langue ou nouveau paramètre de configuration public : généralement `MINOR`.
- Suppression d’une langue, changement incompatible de clé i18n publique, changement majeur de configuration attendue ou rupture du design system consommé par plusieurs apps : `MAJOR` si cela casse des intégrations ou usages existants.

## Documentation et SemVer

Toute décision SemVer doit s’appuyer sur la documentation des changements :

- `FUNCTIONAL-SPEC.md` pour le comportement métier ;
- `TECHNICAL-DESIGN.md` pour l’architecture ;
- `API-CONTRACT.md` pour les changements d’API ;
- `DATA-MODEL.md` pour les migrations ;
- `TEST-PLAN.md` pour la couverture de validation.

Un breaking change non documenté doit bloquer la release.

> Les standards non négociables d'`AGENTS.md` ne changent pas la version mais bloquent la release s'ils sont violés (`scripts\check-standards.bat`).
