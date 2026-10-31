# WORKFLOW IA — Procédure détaillée

> À lire seulement en cas de doute sur la procédure : le résumé opérationnel est
> dans `AGENTS.md`, sections « Qualifier la demande » à « Réponse finale ».

## 1. Démarrer

- [ ] Mode et gabarit déterminés (`AGENTS.md`, sections « Qualifier la demande », « Routage » et « Gabarits »).
- [ ] Ticket repris ou créé : `TEMPLATE-ticket-lite.md` (LÉGER) ou
      `TEMPLATE-ticket-action.md` (STANDARD).
- [ ] Demande macro ou > 2–3 j : passer en mode Project Manager, ne pas coder.

## 2. Comprendre

Noter dans le ticket : besoin, résultat attendu, critères d'acceptation,
modules impactés, hypothèses, risques. En STANDARD, ajouter dépendances,
profil recommandé et estimation.

## 3. Analyser l'existant (ciblé)

Rechercher avant d'ouvrir : `rg -n "<symbole|endpoint|clé i18n>"`
(ou `findstr /s /n /i` si rg est absent).
Inspecter seulement ce que le changement touche : code, tests, contrats API,
DB et migrations, flux UI, sécurité, config, CI/CD.

## 4. Implémenter

- changements minimaux, cohérents avec l'existant ;
- responsabilités séparées (`AGENTS.md` S7) ;
- pas de refactoring massif ni de suppression sans justification ;
- aucun changement de contrat silencieux.

## 5. Vérifier

| Stack | Commandes | Tests attendus selon impact |
|---|---|---|
| Spring Boot | `mvnw.cmd test` ; `mvnw.cmd clean verify` si build/CI | domaine/service, controller, AuthN/AuthZ, repository/migration |
| Angular | `npm run lint` ; `npm run test` ; `npm run build` | composants, services, guards, interceptors ; XSS/CSP si HTML |
| Flutter | `flutter analyze` ; `flutter test` | providers, use cases, widgets ; secure storage |
| Tous | `scripts\check-standards.bat .` | standards non négociables d'`AGENTS.md` |
| CI/CD | pipeline lint/build/test, scans | migrations testées, rollback documenté si risqué |

## 6. Clôturer

- [ ] Ticket coché, reste à faire explicite.
- [ ] Une ligne à jour dans `PROJECT-TRACKING.md`.
- [ ] `CHANGELOG.md` si changement notable (API, DB, UI, config, exploitation).
- [ ] ADR si décision structurante ; `docs/pm/` si impact planning.
- [ ] Bump SemVer proposé si livrable.
- [ ] Réponse finale au format court (`AGENTS.md` (section « Réponse finale »)).
