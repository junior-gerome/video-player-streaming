# Revue — Architecture et découpage (commun)

> Charger seulement si le diff touche ce domaine. Marquer ✅ / ❌ / ➖ (justifié). Bloquants : `BLOQUANTS.md`.
> Règles : `docs/standards/ARCHITECTURE-SOLID-RESPONSIBILITY-STANDARDS.md`.
> Détail par technologie : `BACKEND-SPRING.md`, `FRONT-ANGULAR.md`, `FRONT-UI.md`.

## Responsabilités

- [ ] Le backend porte la vérité métier : validation, décisions, autorisations, calculs, persistance.
- [ ] Aucune règle métier critique n'existe uniquement côté Angular ou Flutter.
- [ ] Contrôleurs sans logique métier ; aucun appel direct à un repository (hors `existsById` en validator), à du SQL ou à un client HTTP.
- [ ] Les services portent l'orchestration, les règles et les transactions.
- [ ] Composants Angular et widgets Flutter orientés présentation, découpés en éléments réutilisables.

## SOLID et abstractions

- [ ] Une responsabilité par classe, composant ou widget ; pas de classe « dieu ».
- [ ] Interfaces petites et orientées usage.
- [ ] Le CRUD standard utilise les composants génériques (`AbstractController`, `IAbstractService`, `CrudService`) au lieu d'être réécrit.
- [ ] Toute autre abstraction, généricité ou héritage répond à un besoin réel ; profondeur d'héritage limitée à 2 niveaux sous le core-module.

## Tailles

- [ ] Classe, composant ou widget : alerte au-delà de 300 lignes, rejet au-delà de 500 (sauf ADR temporaire).
- [ ] Template Angular : 300 lignes maximum.
- [ ] Méthode : 40 lignes maximum ; 5 paramètres maximum ; complexité cyclomatique 10 maximum.

## Structure (mobile)

- [ ] Flutter : découpage `domain` / `data` / `presentation` par feature ; interfaces de repository dans `domain`.
- [ ] Flutter : widget de plus de 200 lignes découpé en sous-widgets (classes, pas méthodes de construction).
