# Revue — Documentation et gouvernance

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.

## Documentation du code et de l'API

- [ ] API publiques documentées (OpenAPI généré par SpringDoc, `@Tag` et descriptions à jour).
- [ ] Commentaires expliquant le **pourquoi**, pas le **quoi**.
- [ ] Code déprécié annoté `@Deprecated` avec solution de remplacement et échéance.
- [ ] README à jour (installation, architecture, contribution).
- [ ] Décision structurante tracée par un ADR.
- [ ] `docs/ERROR-CODES.md` à jour si un code d'erreur est ajouté ou modifié.

## Documentation First (gabarit STANDARD)

- [ ] Spécification fonctionnelle et conception technique présentes avant ou avec le développement.
- [ ] Critères d'acceptation documentés et alignés avec les tests.
- [ ] Contrat d'API documenté si endpoint, DTO ou erreur modifiés.
- [ ] Modèle de données et migrations documentés si la base est touchée.
- [ ] Impacts de configuration documentés (`application.yml`, proxy Angular, réglages applicatifs, Flutter).
- [ ] Impacts UI, i18n, thème et branding documentés si Angular ou Flutter sont touchés.
- [ ] Changelog et décision SemVer à jour si le changement est livrable.

## Conformité

- [ ] Inventaire des composants logiciels (SBOM) produit avec chaque release.
- [ ] Licences des dépendances compatibles avec l'usage du projet.
- [ ] Accessibilité WCAG 2.2 AA pour le web, recommandations de plateforme pour le mobile.
- [ ] Protection des données dès la conception : minimisation, finalité, consentement.
