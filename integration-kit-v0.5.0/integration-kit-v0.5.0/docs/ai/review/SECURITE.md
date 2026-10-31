# Revue — Sécurité

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.

## Authentification et autorisation

- [ ] Accès refusé par défaut ; chaque endpoint est protégé explicitement.
- [ ] Contrôles fins par méthode (`@PreAuthorize`) ou par route (guards Angular).
- [ ] Moindre privilège pour les utilisateurs et les comptes de service.
- [ ] JWT vérifié : émetteur, audience, expiration, algorithme de signature, date de début de validité.
- [ ] Jetons de rafraîchissement renouvelés à chaque usage ; délai d'expiration de session appliqué.
- [ ] Opérations d'administration ou sensibles : authentification renforcée si le projet l'exige.
- [ ] Verrouillage après échecs répétés et limitation de débit sur connexion et réinitialisation de mot de passe.

## Protection des données

- [ ] TLS 1.2 minimum, TLS 1.3 privilégié.
- [ ] Données sensibles chiffrées au repos.
- [ ] Mots de passe hachés en Argon2id ou bcrypt (coût ≥ 12), jamais MD5 ni SHA seul.
- [ ] Données personnelles masquées dans les logs et les réponses d'erreur.
- [ ] Durées de conservation et droit à l'effacement pris en compte (RGPD).
- [ ] Fichiers envoyés contrôlés (type, taille, contenu) avant traitement.

## Secrets

- [ ] Aucun secret dans le code, les fichiers de configuration versionnés ou les valeurs par défaut.
- [ ] Secrets fournis par un coffre ou par les variables sécurisées de la CI.
- [ ] Fichiers `.env` ignorés par Git ; un `.env.example` sans valeur réelle existe.
- [ ] Aucun secret dans les logs, même en niveau DEBUG.

## En-têtes et navigateur (web)

- [ ] `Content-Security-Policy` sans `unsafe-inline` ni `unsafe-eval` injustifiés.
- [ ] `Strict-Transport-Security` (max-age ≥ 31536000), `X-Content-Type-Options: nosniff`, `X-Frame-Options` ou `frame-ancestors`.
- [ ] `Referrer-Policy: strict-origin-when-cross-origin` ou plus strict.
- [ ] CORS limité à des origines explicites dans `WebConfig`, jamais `*` en production.
- [ ] Aucune donnée sensible dans `localStorage` ; pas de contournement du sanitizer Angular sans justification.

## Mobile (Flutter)

- [ ] Builds de production obfusqués (`--obfuscate --split-debug-info`) ; R8 activé sur Android.
- [ ] Données sensibles dans `flutter_secure_storage`, jamais `SharedPreferences`.
- [ ] Épinglage de certificat pour les applications à forte exigence de sécurité.
- [ ] Aucun log de donnée sensible en production.
