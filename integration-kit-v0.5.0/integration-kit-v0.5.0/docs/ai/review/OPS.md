# Revue — Observabilité, performance, CI/CD

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.

## Logs

- [ ] Logs structurés (JSON) en production : horodatage, niveau, identifiant de corrélation, service, message.
- [ ] Identifiant de corrélation propagé entre services (en-têtes HTTP, messages).
- [ ] Niveaux adaptés : ERROR (action requise), WARN (dégradé), INFO (événement métier), DEBUG désactivé en production.
- [ ] Aucune donnée personnelle, aucun secret ni jeton, aucune trace complète dans les logs.

## Métriques et traces

- [ ] Latence (p50, p95, p99), taux d'erreur et débit suivis pour chaque endpoint (Micrometer / Actuator).
- [ ] Pool de connexions, threads (virtuels compris), mémoire et CPU surveillés.
- [ ] Traçage distribué (OpenTelemetry) propagé à travers HTTP, messages et traitements asynchrones.
- [ ] Requêtes lentes (plus de 200 ms) repérables dans les traces.

## Santé et alertes

- [ ] Sondes `liveness` et `readiness` exposées ; la `readiness` inclut base et dépendances critiques.
- [ ] Alertes définies (taux d'erreur, latence, disque, expiration de certificat), chacune avec sa procédure.

## Performance

- [ ] Requêtes profilées ; aucun parcours complet de grande table sans index.
- [ ] Threads virtuels activés pour les traitements d'entrées/sorties (`spring.threads.virtual.enabled: true`).
- [ ] Angular : écrans chargés à la demande, `@defer` pour les composants lourds, taille des bundles suivie.
- [ ] Réponses HTTP compressées ; images optimisées.
- [ ] Flutter : reconstructions de widgets limitées (`const`), aucune opération bloquante sur le fil d'interface.

## CI/CD et déploiement

- [ ] Pipeline complet : lint → build → tests unitaires → tests d'intégration → `scripts\check-standards.bat` → analyse de sécurité → seuil de couverture.
- [ ] Images Docker multi-étapes, utilisateur non root, analysées (Trivy ou équivalent).
- [ ] Configuration propre à chaque environnement externalisée.
- [ ] Migrations de base jouées avant le démarrage, avec plan de retour arrière.
- [ ] Procédure de retour arrière documentée et testée ; tag Git pour chaque release.
