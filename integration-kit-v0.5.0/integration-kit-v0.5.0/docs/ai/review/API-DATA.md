# Revue — API, contrats et données

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.

## Contrat HTTP

- [ ] Routes préfixées `/api/v{n}/` ; changement non rétrocompatible = nouvelle version d'API + bump MAJOR.
- [ ] Réponses enveloppées dans `ApiResponse<T>` ; toute erreur porte un `errorCode` `{PREFIXE}-{VAL|MET|SEC|TEC}-nnn`.
- [ ] Tout nouveau code d'erreur est ajouté à `docs/ERROR-CODES.md` et traduit côté front (`fr`, `en`).
- [ ] Codes HTTP corrects : 200, 201 (création), 204 (suppression), 400, 401, 403, 404, 409, 422, 429, 500.
- [ ] Listes volumineuses paginées (`Pageable` → `PageResponseDto<T>`) ; `page >= 0` et `size > 0` contrôlés.
- [ ] Nommage REST cohérent (noms au pluriel, casse homogène).

## DTO et modèles

- [ ] Aucune entité JPA exposée ; DTO classés Basic / Full / Custom selon l'usage.
- [ ] BasicDTO sans sous-objet ni liste ; FullDTO étend BasicDTO ; CustomDTO justifié par un écran réel (`record` Java 21).
- [ ] Aucune annotation JPA dans un DTO.
- [ ] Modèles Angular (`…BasicModel`, `…FullModel`, `…CustomModel`) et enums identiques au contrat backend.

## Validation des entrées

- [ ] `@Valid` sur tous les DTO de requête ; messages d'erreur exploitables.
- [ ] Validation défensive des services dans l'ordre : nullité → existence → règle métier.
- [ ] Fichiers envoyés contrôlés en type, taille et contenu (pas seulement l'extension).
- [ ] Aucune saisie utilisateur brute vers SQL, commandes système, chemins de fichiers ou moteurs de gabarits.

## Base de données

- [ ] Requêtes paramétrées uniquement ; JPQL `@Query` + `@Param`.
- [ ] N+1 traité (`JOIN FETCH`, `@EntityGraph`).
- [ ] Index sur les clés étrangères et les colonnes filtrées ou triées.
- [ ] Pagination faite en base, pas en mémoire.
- [ ] Pool de connexions (HikariCP) paramétré explicitement.

## Migrations

- [ ] Migrations versionnées (Flyway ou Liquibase), jouées en CI sur une vraie base (Testcontainers).
- [ ] Compatibles en avant : ajouter avant de supprimer, migration en deux temps pour un renommage.
- [ ] Changement destructif accompagné d'un plan de retour arrière.

## Cache

- [ ] Clés déterministes (version, locataire si besoin), invalidation documentée et testée, durée de vie définie.
- [ ] Aucune donnée sensible en cache partagé ou côté client.
