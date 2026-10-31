# Revue — Backend Spring Boot (Java 21 / Spring Boot 4.1 / Lombok)

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.
> Règles : `docs/standards/BACKEND-SPRING-STANDARDS.md`.

## Socle

- [ ] Java 21 (`java.version`), `spring-boot-starter-parent` 4.1.x, Maven wrapper.
- [ ] Aucune version surchargée d'une dépendance gérée par le BOM Spring Boot (sauf ADR sécurité).
- [ ] Configuration en `application.yml` + profils ; aucun secret par défaut.

## Modules Maven

- [ ] `core-module` ne dépend d'aucun autre module du projet.
- [ ] Un module de domaine ne dépend que du `core-module`.
- [ ] Le `web-service-module` n'importe que les interfaces de service et les DTO des modules de domaine.
- [ ] Aucun import d'une classe interne (entité, repository, implémentation) d'un autre module.

## Couches et composants génériques

- [ ] Entité implémente `IEntity` ; BasicDTO implémente `IDTO` ; FullDTO étend BasicDTO.
- [ ] Contrôleur CRUD hérite d'`AbstractController` sans redéfinir le CRUD ; pas de `@CrossOrigin` (CORS dans `WebConfig`).
- [ ] Service spécifique étend `IAbstractService` ; implémentation `service/impl/{PROJET}{Entite}ServiceImpl`.
- [ ] Validation défensive dans l'ordre nullité → existence → règle métier.
- [ ] `@Transactional(readOnly = true)` en lecture, `@Transactional` en écriture, uniquement sur les services.
- [ ] Mapping isolé (MapStruct ou méthodes dédiées), jamais au milieu de la logique métier.
- [ ] Repository limité à la persistance ; `Optional` pour les recherches par identifiant.

## Lombok

- [ ] Aucune entité JPA avec `@Data`, `@Value` ou `@EqualsAndHashCode` / `@ToString` incluant les relations.
- [ ] Relations d'entité marquées `@ToString.Exclude` et `@EqualsAndHashCode.Exclude`.
- [ ] BasicDTO et FullDTO en `@Data` + `@SuperBuilder` (jamais `@Builder` dans une hiérarchie).
- [ ] Services en `@RequiredArgsConstructor` + champs `private final` ; aucun `@Autowired` sur un champ.
- [ ] Aucun `@SneakyThrows`, `val`, `var` Lombok, `@Cleanup`, `@Synchronized`.
- [ ] `lombok.config` présent à la racine du backend ; Lombok en `provided` et déclaré comme processeur d'annotations (avant MapStruct).

## Nommage

- [ ] Classes `{PROJET}{Entite}`, interfaces `I{PROJET}{Entite}…`, DTO suffixés `BasicDTO` / `FullDTO` / `CustomDTO`.
- [ ] Méthodes en camelCase, verbes explicites.
- [ ] Packages : `controller`, `service/impl`, `repository`, `entity`, `dto/basic|full|custom`, `mapper`.

## Exceptions et sécurité

- [ ] Toute exception appartient à une catégorie (Validation / Métier / Sécurité / Technique) avec un seul code HTTP.
- [ ] Toute `BusinessException` porte un code `{PREFIXE}-MET-nnn` ; l'`ApiResponse` d'erreur contient `errorCode`.
- [ ] Aucune trace technique renvoyée au client (`@RestControllerAdvice`).
- [ ] Utilisateur courant lu via `UserContextHolder`, jamais depuis un paramètre client.
- [ ] Accès à la ressource d'un tiers vérifié par un service d'accès dédié.
- [ ] Aucun mot de passe, jeton complet ou donnée personnelle dans les logs.

## Tests

- [ ] Chaque méthode de service à règle : un test nominal + un test par exception levée.
- [ ] Chaque `@Query` personnalisée : un test d'intégration Testcontainers.
- [ ] Contrôleur spécifique : tests `@WebMvcTest` de ses seuls endpoints (le CRUD générique est testé une fois).
