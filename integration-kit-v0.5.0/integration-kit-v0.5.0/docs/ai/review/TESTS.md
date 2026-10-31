# Revue — Tests

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.

## Couverture et qualité

- [ ] Logique métier (services) : au moins 80 % de lignes couvertes, cible 95 %.
- [ ] Chemins de sécurité (authentification, autorisation) : 100 %, cas autorisé et refusé.
- [ ] Contrôleurs et composants : au moins 70 %, cible 85 %.
- [ ] Chaque test contient au moins une assertion utile ; structure Préparer → Agir → Vérifier.
- [ ] Noms explicites : `should_<résultat>_when_<condition>`.
- [ ] Aucun `@Disabled` / `skip` sans ticket lié.
- [ ] Code généré par Lombok exclu de la couverture (`lombok.addLombokGeneratedAnnotation`).

## Tests unitaires

- [ ] Règles métier testées sans entrée/sortie réelle (dépendances simulées).
- [ ] Cas limites : null, vide, bornes, formats invalides.
- [ ] Chaque exception levée par un service a son test (type et `errorCode`).
- [ ] Suite unitaire exécutée en moins de 30 secondes.

## Tests d'intégration (backend)

- [ ] Endpoints testés en `@WebMvcTest` (contrat HTTP, statut, `ApiResponse`).
- [ ] Accès base testé avec Testcontainers sur le même moteur que la production.
- [ ] Appels externes simulés (WireMock ou équivalent) ; courtiers de messages via Testcontainers.
- [ ] Suite d'intégration exécutée en moins de 10 minutes.

## Tests front et mobile

- [ ] Angular : services avec `HttpTestingController` ; composants sur leurs entrées (`input()`), sorties et états chargement / vide / erreur.
- [ ] Angular : guards et intercepteurs testés en cas autorisé et refusé.
- [ ] Flutter : providers testés avec dépendances simulées ; tests d'image de référence pour les composants du design system.

## Tests de bout en bout

- [ ] Parcours critiques couverts (connexion, flux métier principal).
- [ ] Exécutés en CI sur l'environnement de recette ; régression visuelle sur les écrans clés.
