# Architecture, SOLID et séparation des responsabilités — principes communs

> Principes valables pour Spring Boot, Angular et Flutter. Les règles propres
> à chaque technologie sont dans `BACKEND-SPRING-STANDARDS.md`,
> `FRONTEND-ANGULAR-STANDARDS.md` et `MOBILE-FLUTTER-STANDARDS.md`.
> À charger pour une décision de découpage, une nouvelle couche ou un refactoring.

## Le backend est maître de la vérité métier

- Le backend valide, calcule, autorise, persiste et décide.
- Angular et Flutter affichent, collectent les saisies, guident l'utilisateur
  et gèrent l'état de présentation.
- Une validation côté client est une aide ergonomique ; elle ne remplace
  jamais la validation backend.
- Aucune règle métier critique n'existe uniquement côté client ; toute règle
  reproduite côté client est une aide d'affichage, pas une source de vérité.

## SOLID

| Principe | Règle concrète | Rejet si |
|---|---|---|
| SRP | Une classe, un composant ou un widget a une seule raison de changer. | Une classe mêle validation, persistance, mapping et appel externe. |
| OCP | Les variations métier passent par interfaces, stratégies ou handlers dédiés. | Un gros `if/else` central est modifié à chaque nouveau cas. |
| LSP | Une implémentation respecte le contrat de son abstraction. | Une sous-classe change le comportement attendu. |
| ISP | Interfaces petites, orientées usage. | Une interface impose des méthodes inutiles. |
| DIP | Les couches hautes dépendent d'abstractions. | Un contrôleur dépend d'un repository ou d'un client HTTP. |

## Généricité, héritage, composition

- **Généricité validée par l'équipe** : les composants CRUD du `core-module`
  (`AbstractController`, `IAbstractService`, `IEntity`, `IDTO`) côté backend
  et `CrudService` côté Angular. Ils couvrent le CRUD technique ; les règles
  métier restent dans les services spécifiques.
- Toute **autre** abstraction générique exige un besoin réel (duplication
  constatée, variation, testabilité) ; pas de classe `AbstractManager` ou de
  `BaseService` fourre-tout, pas de types bruts.
- Composition préférée à l'héritage ; héritage limité à une relation « est un »
  stable ; profondeur maximale : 2 niveaux sous l'abstraction du core-module,
  sauf ADR.

## Limites de taille

| Élément | Limite dure | Seuil d'alerte |
|---|---:|---:|
| Classe Java, service, contrôleur | 500 lignes | 300 lignes |
| Composant Angular (TS) | 500 lignes | 300 lignes |
| Template Angular (HTML) | 300 lignes | 200 lignes |
| Widget Flutter | 500 lignes | 250 lignes |
| Méthode / fonction | 40 lignes | 25 lignes |
| Paramètres de méthode | 5 | 3 |
| Profondeur d'imbrication | 3 niveaux | 2 niveaux |
| Complexité cyclomatique | 10 | 5 |

Au seuil d'alerte, proposer une extraction ; au-delà de la limite dure, la
merge request est refusée sauf ADR temporaire.

## Avant de coder : questions obligatoires

- Quelle couche porte la responsabilité ?
- Quel contrat est exposé (DTO, modèle, endpoint) ?
- Quel service porte l'orchestration et la transaction ?
- Quelle règle appartient au backend, quelle partie est présentation ?
- Quels tests prouvent la règle métier ?
- Quels composants réutilisables existent déjà ?
- L'abstraction envisagée est-elle nécessaire ou spéculative ?

## Tests : répartition

- Règles métier : testées côté backend, dans les services.
- Contrôleurs : contrat HTTP et délégation uniquement.
- Mappers : testés si le mapping n'est pas trivial.
- Composants Angular et widgets Flutter : affichage, interactions, états
  chargement / vide / erreur.
- Une validation client ne remplace jamais un test backend.

## Rejet immédiat en revue

- Logique métier dans un contrôleur, une entité ou un composant d'affichage.
- Règle métier critique uniquement côté client.
- Contrôleur qui appelle un repository (hors `existsById` dans un validator),
  un client HTTP ou du SQL.
- Classe, composant ou widget au-delà de 500 lignes.
- Interface fourre-tout ; abstraction ou héritage sans besoin réel.
- Entité JPA exposée par l'API.
- CRUD standard réécrit au lieu d'utiliser les composants génériques.

## Résumé

```text
Backend = vérité métier, validation, sécurité, persistance, décision.
Client  = affichage, saisie, expérience, état de présentation.
Contrôleur = délégation.   Service = orchestration + transaction + règles.
Repository = persistance.  Composant / widget = présentation.
```
