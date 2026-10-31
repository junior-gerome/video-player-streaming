# Standard Backend — Java 21 / Spring Boot 4.1 / Maven / Lombok

> Standard d'équipe du backend en couches (Controller / Service / Repository /
> Entity / DTO). Il est chargé en mode Engineering backend.
> Listes de contrôle de revue : `docs/ai/review/BACKEND-SPRING.md`.
> Remplacer `{PROJET}` par le préfixe du projet (ex. `TNA`, `GIM`) et
> `{Entite}` par le nom métier.

## Socle technique

| Élément | Version de référence | Règle |
|---|---|---|
| Java | **21 (LTS)** | `<java.version>21</java.version>` dans le `pom.xml` parent |
| Spring Boot | **4.1.x** | `spring-boot-starter-parent` 4.1.x (Spring Framework 7, Spring Security 7, Hibernate 7, Jakarta EE 11, Jackson 3) |
| Build | Maven + wrapper | `mvnw.cmd` / `mvnw` versionnés ; Gradle interdit |
| Lombok | 1.18.46 ou supérieure | version gérée par le BOM Spring Boot (propriété `lombok.version`) |
| Documentation API | SpringDoc OpenAPI 3.x | `springdoc-openapi-starter-webmvc-ui` |
| Mapping | MapStruct 1.6.x (optionnel) | processeur déclaré après Lombok (voir « Lombok ») |
| Tests | JUnit Jupiter, Mockito, Testcontainers | versions gérées par le BOM Spring Boot |

- Les versions des dépendances gérées par le BOM Spring Boot ne sont jamais
  surchargées dans les `pom.xml`, sauf correctif de sécurité tracé par un ADR.
- Jackson 3 : les annotations restent dans `com.fasterxml.jackson.annotation`,
  le reste de Jackson est dans les paquets `tools.jackson`.
- Configuration : `application.yml` + profils, voir `CONFIGURATION-STANDARDS.md`.

### Apports Java 21 à utiliser

- `record` pour les DTO Custom en lecture seule et les projections JPA.
- `switch` avec pattern matching et `sealed` pour les hiérarchies fermées
  (statuts, résultats de règles).
- Blocs de texte `"""` pour les requêtes JPQL longues.
- Threads virtuels : `spring.threads.virtual.enabled: true` pour les
  services à forte charge d'entrées/sorties.
- `var` autorisé pour une variable locale dont le type est évident à la lecture.

## Organisation Maven multi-modules

| Module | Contenu | Dépend de |
|---|---|---|
| `{projet}-core-module` | `ApiResponse`, `PageResponseDto`, exceptions catégorisées, `IEntity`, `IDTO`, `AbstractController`, `IAbstractService`, base sécurité JWT | aucun module du projet |
| `{projet}-{domaine}-module` | entités JPA, enums, repositories, services, DTO du domaine | `core-module` uniquement |
| `{projet}-web-service-module` | point d'entrée Spring Boot, contrôleurs REST, configurations (CORS, sécurité, OpenAPI), gestionnaire global d'exceptions | modules de domaine (et `core-module` par transitivité) |

- Le `web-service-module` n'utilise que les interfaces de service et les DTO
  exposés d'un module de domaine, jamais ses entités, repositories ou implémentations.
- Deux modules de domaine ne dépendent jamais l'un de l'autre : l'interaction
  passe par une interface de service injectée.

## Architecture en couches : règles d'or

1. **Un package = une responsabilité** technique et fonctionnelle.
2. **Pas d'appel Controller → Repository**. Seule exception : un contrôle
   trivial d'existence (`existsById`) dans un validator dédié.
3. **DTO obligatoires aux frontières** : aucune entité JPA ne sort de la
   couche service.
4. **Zéro logique métier dans les contrôleurs et les entités** : 100 % de la
   logique métier transactionnelle est dans les services.
5. **Transactions déclaratives** : `@Transactional(readOnly = true)` en
   lecture, `@Transactional` en écriture, uniquement sur les services.

## Composants génériques du core-module

Pour toute entité CRUD standard, utiliser ces composants au lieu de réécrire
le CRUD. C'est la généricité validée par l'équipe : elle couvre le CRUD
technique, jamais une règle métier.

```java
public interface IEntity extends Serializable {
    Integer getId();
    void setId(Integer id);
    LocalDate getDateCreation();
    void setDateCreation(LocalDate dateCreation);
    LocalDate getDateModification();
    void setDateModification(LocalDate dateModification);
}

public interface IDTO {
    Integer getId();
    void setId(Integer id);
    LocalDate getDateCreation();
    void setDateCreation(LocalDate dateCreation);
    LocalDate getDateModification();
    void setDateModification(LocalDate dateModification);
}

public interface IAbstractService<E extends IEntity, B extends IDTO, F extends B,
        R extends JpaRepository<E, Integer>> {
    F create(F dto) throws BusinessException;
    F update(F dto) throws BusinessException;
    void deleteById(Integer id) throws BusinessException;
    List<B> findAll();
    F findById(Integer id) throws BusinessException;
    boolean ifEntityExistById(Integer id) throws BusinessException;
}
```

`AbstractController<E, B, F, R, S extends IAbstractService<E, B, F, R>>`
fournit le CRUD standard :

| Requête | Méthode | Réponse |
|---|---|---|
| `POST` | `create(FullDTO)` | 201 |
| `PUT /{id}` | `update(FullDTO)` | 200 |
| `DELETE /{id}` | `deleteById(id)` | 204 |
| `GET` | `findAll()` | 200, liste de BasicDTO |
| `GET /{id}` | `findById(id)` | 200, FullDTO |
| `GET /{id}/exists` | `existsById(id)` | 200, booléen |

```java
@RestController
@RequestMapping("/api/v1/{ressource}")
@Tag(name = "{Ressource}", description = "Gestion de {ressource}")
public class {PROJET}{Entite}Controller extends AbstractController<{PROJET}{Entite},
        {PROJET}{Entite}BasicDTO, {PROJET}{Entite}FullDTO, I{PROJET}{Entite}DAO, I{PROJET}{Entite}Service> {

    public {PROJET}{Entite}Controller(I{PROJET}{Entite}Service service) {
        super(service);
    }

    // Uniquement les endpoints hors CRUD standard (recherche filtrée, actions métier)
}
```

- Tout contrôleur d'entité CRUD standard hérite d'`AbstractController` et ne
  redéfinit jamais le CRUD.
- Le CORS n'est jamais déclaré par `@CrossOrigin` : il est centralisé dans une
  classe `WebConfig` du `web-service-module`.
- Le service spécifique `I{PROJET}{Entite}Service` étend `IAbstractService` et
  ajoute ses méthodes métier ; son implémentation est
  `service/impl/{PROJET}{Entite}ServiceImpl`.

## Implémentation des services

1. **Validation défensive en début de méthode, dans cet ordre** :
   1. nullité et syntaxe → `IllegalArgumentException` ;
   2. existence en base → `ResourceNotFoundException` ;
   3. règles métier → `BusinessException`.
2. **Transactions** : `@Transactional(readOnly = true)` sur les lectures,
   `@Transactional` sur les écritures.
3. **Mapping isolé** : mappers MapStruct ou méthodes privées dédiées ; relations
   traitées défensivement (`Optional`, filtres de flux).
4. **Logs** : `@Slf4j`, messages à paramètres `{}` ; jamais de mot de passe,
   clé, jeton complet ou donnée personnelle ; `log.warn` sur échec
   d'authentification ou accès refusé.

## Repositories

- Héritent de `JpaRepository<Entity, Integer>` ; uniquement persistance et requêtes.
- N+1 évité par `JOIN FETCH` ou `@EntityGraph`.
- `Optional<T>` pour toute recherche par attribut identifiant.
- Requêtes métier en JPQL `@Query` + `@Param` ; projections en `record`.

## DTO : Basic, Full, Custom

| Type | Contenu | Usage |
|---|---|---|
| `{PROJET}{Entite}BasicDTO` | implémente `IDTO` ; scalaires et identifiants, ni sous-objet ni liste | listes (`findAll`) |
| `{PROJET}{Entite}FullDTO` | étend BasicDTO ; ajoute objets liés et listes | détail, création, modification |
| `{PROJET}{Entite}{Usage}CustomDTO` | hors hiérarchie ; besoin d'écran précis (agrégat, export, tableau de bord) | `record` Java 21 recommandé |

Aucune annotation JPA dans un DTO. Annotations autorisées : Lombok (voir
ci-dessous), Jackson, Bean Validation (`@NotNull`, `@Size`…).

## Lombok

Lombok est **obligatoire** pour le code répétitif (accesseurs, constructeurs,
logger) et **encadré** pour éviter ses pièges avec JPA et l'héritage.

### Annotations par type de classe

| Classe | Annotations Lombok | Interdit |
|---|---|---|
| Entité JPA | `@Getter`, `@Setter`, `@NoArgsConstructor` ; `@ToString.Exclude` et `@EqualsAndHashCode.Exclude` sur chaque relation | `@Data`, `@Value`, `@EqualsAndHashCode` / `@ToString` sans exclusion des relations, `@Builder` seul |
| BasicDTO | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@SuperBuilder` | `@Builder` (incompatible avec l'héritage Full → Basic) |
| FullDTO | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@SuperBuilder` | `@Builder` |
| CustomDTO | aucune (`record` Java 21) ou `@Value` + `@Builder` | `@Data` |
| Service, composant Spring | `@RequiredArgsConstructor` (champs `private final`), `@Slf4j` | `@Autowired` sur un champ |
| Contrôleur héritant d'`AbstractController` | `@Slf4j` si besoin ; constructeur explicite appelant `super(service)` | `@RequiredArgsConstructor` (ne sait pas appeler `super`) |

- Si l'égalité d'une entité est nécessaire : `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`
  avec `@EqualsAndHashCode.Include` sur l'identifiant uniquement.
- Valeurs par défaut avec un builder : `@Builder.Default`.
- Interdits partout : `@SneakyThrows`, `val`, `var` de Lombok, `@Cleanup`,
  `@Synchronized`. Seule annotation expérimentale autorisée : `@SuperBuilder`.

### Fichier `lombok.config` (racine du dépôt backend)

```properties
config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
lombok.equalsAndHashCode.callSuper = call
lombok.toString.callSuper = call
lombok.val.flagUsage = error
lombok.var.flagUsage = error
lombok.sneakyThrows.flagUsage = error
lombok.cleanup.flagUsage = error
lombok.synchronized.flagUsage = error
```

`addLombokGeneratedAnnotation` exclut le code généré de la couverture JaCoCo ;
`callSuper = call` fait inclure les champs du BasicDTO dans `equals`,
`hashCode` et `toString` du FullDTO.
Modèle : `docs/templates/spring/lombok.config.example`.

### Déclaration Maven

Lombok est déclaré `provided` et comme processeur d'annotations explicite.
Avec MapStruct, l'ordre est obligatoire : Lombok, puis
`lombok-mapstruct-binding`, puis MapStruct.
Modèle complet : `docs/templates/spring/pom-java21-lombok.xml.example`.

## Convention de nommage

| Élément | Format | Exemple |
|---|---|---|
| Classe métier | `{PROJET}{Entite}` | `TNAEmployee` |
| Interface | `I{PROJET}{Entite}Service`, `I{PROJET}{Entite}DAO` | `ITNAEmployeeService` |
| DTO | `{PROJET}{Entite}BasicDTO`, `…FullDTO`, `…{Usage}CustomDTO` | `TNAEmployeePlanningCustomDTO` |
| Implémentation | `{PROJET}{Entite}ServiceImpl` | `TNAEmployeeServiceImpl` |
| Méthode | camelCase, verbe explicite | `findByStatutActif`, `mapToFullDto` |

## Réponses et exceptions

Toute réponse est enveloppée dans `ApiResponse<T>` :

```json
{ "success": true, "message": "Opération effectuée avec succès",
  "errorCode": null, "data": { }, "timestamp": "2026-09-09T09:30:00" }
```

```json
{ "success": false, "message": "L'employé est déjà affecté à ce planning",
  "errorCode": "TNA-MET-004", "data": null, "timestamp": "2026-09-09T09:30:00" }
```

Le champ `errorCode` est **obligatoire** dans toute réponse d'erreur : le
frontend s'en sert pour afficher un message traduit.

| Catégorie | Exception | HTTP | Code |
|---|---|---|---|
| Validation | `IllegalArgumentException`, erreurs Bean Validation | 400 | `{PREFIXE}-VAL-nnn` |
| Métier | `BusinessException` | 422 | `{PREFIXE}-MET-nnn` |
| Métier | `ResourceNotFoundException` | 404 | `{PREFIXE}-MET-nnn` |
| Sécurité | `AccessDeniedException` (Spring Security) | 403 | `{PREFIXE}-SEC-nnn` |
| Sécurité | `UnauthorizedException` | 401 | `{PREFIXE}-SEC-nnn` |
| Technique | `TechnicalException` | 500 | `{PREFIXE}-TEC-nnn` |

- Toute nouvelle exception est rattachée à une catégorie et à un seul code HTTP.
- Un gestionnaire global `@RestControllerAdvice` (web-service-module) produit
  l'`ApiResponse` d'erreur ; aucune trace technique n'est renvoyée au client.
- Chaque code est recensé dans `docs/ERROR-CODES.md` (code, catégorie, HTTP,
  clé i18n front) ; ce catalogue est partagé avec le frontend.

## Sécurité et contexte utilisateur

- Utilisateur courant : `UserContextHolder.getCurrentUserId()` depuis le JWT,
  jamais depuis un paramètre client non vérifié.
- Accès aux ressources d'un tiers : service dédié
  (`{Entite}AccessService.validateAccess(targetUserId, currentUserId)`).
- Rôles bruts du fournisseur d'identité normalisés à un seul endroit.
- `@Valid` systématique sur les DTO de requête ; limitation de débit sur
  connexion et réinitialisation de mot de passe si le volume le justifie.

## Pagination

- Toute liste non bornée par nature est paginée (`Pageable`).
- Réponse : `PageResponseDto<T>` (contenu, page, taille, total d'éléments,
  total de pages), jamais une `List<T>` brute.
- Le contrôleur vérifie `page >= 0` et `size > 0`.

## Versionnement d'API

- Toutes les routes sont préfixées `/api/v{n}/`.
- Tout changement non rétrocompatible d'un DTO exposé crée `/api/v{n+1}/`
  (et un bump MAJOR), jamais une modification silencieuse.

## Tests

- Services : un test du cas nominal et un test par exception levée pour toute
  méthode portant une règle ou une validation.
- Repositories : test d'intégration Testcontainers pour toute `@Query`
  personnalisée, en particulier les `JOIN FETCH`.
- CRUD générique : testé une fois sur les composants du core-module ; un
  contrôleur spécifique ne teste que ses propres endpoints (`@WebMvcTest`).

## Structure des packages d'un module

```text
{package-racine}.{module}
 ├── controller          (web-service-module)
 ├── service
 │    └── impl
 ├── repository
 ├── entity
 ├── dto
 │    ├── basic
 │    ├── full
 │    └── custom
 └── mapper              (si MapStruct)
```

Les composants transverses (`IEntity`, `IDTO`, `AbstractController`,
`IAbstractService`, `ApiResponse`, `PageResponseDto`, exceptions) n'existent
que dans le `core-module`.
