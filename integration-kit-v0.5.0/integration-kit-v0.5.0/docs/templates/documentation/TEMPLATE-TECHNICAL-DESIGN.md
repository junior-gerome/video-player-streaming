# TECHNICAL-DESIGN — <Feature name>

## 1. Objectif technique

Décrire l’approche technique retenue.

## 2. Stack concernée

- [ ] Spring Boot
- [ ] Angular
- [ ] Flutter
- [ ] Base de données
- [ ] CI/CD
- [ ] Documentation

## 3. Architecture cible

Décrire les modules, couches et dépendances (standards `BACKEND-SPRING-STANDARDS.md`,
`FRONTEND-ANGULAR-STANDARDS.md`).

```text
Backend  : web-service-module (Controller) → domaine-module (Service → Repository → Entity) ; core-module transverse
Frontend : features/{domaine}/components → services (CrudService) → API /api/v{n}
DTO / modèles concernés : BasicDTO / FullDTO / CustomDTO ↔ BasicModel / FullModel / CustomModel
Codes d'erreur ajoutés : docs/ERROR-CODES.md
```

## 4. Fichiers ou modules impactés

| Module | Fichier | Type d’impact |
|---|---|---|
| | | |

## 5. Contrats API

| Méthode | Endpoint | Request | Response | Erreurs |
|---|---|---|---|---|
| | | | | |

## 6. Modèle de données / migrations

| Élément | Description | Migration requise |
|---|---|---|
| | | Oui / Non |

## 7. Configuration

| Paramètre | Fichier | Valeur / source | Environnement |
|---|---|---|---|
| | `application.yml` / `app.config.ts` / `app_config.dart` | | |

## 8. Sécurité

- [ ] Authentification requise
- [ ] Autorisation / rôle requis
- [ ] Inputs validés
- [ ] Requêtes paramétrées
- [ ] Pas de secret dans le code
- [ ] PII masquée dans logs

## 9. Observabilité

- Logs attendus :
- Métriques :
- Traces :
- Corrélation / request id :

## 10. Tests prévus

| Niveau | Tests attendus | Commande |
|---|---|---|
| Unit | | |
| Integration | | |
| UI / Widget | | |
| E2E | | |

## 11. Impact version / SemVer

| Champ | Valeur |
|---|---|
| Type de bump | Aucun / PATCH / MINOR / MAJOR |
| Justification | |
| Breaking change | Oui / Non |
| Migration requise | Oui / Non |

## 12. Risques techniques

| Risque | Impact | Mitigation |
|---|---|---|
| | | |

## 13. Historique des mises à jour

| Date | Auteur | Changement |
|---|---|---|
| YYYY-MM-DD | | Création |

## Découpage SOLID et responsabilités

### Couche backend

| Élément | Responsabilité | Interface | Implémentation | Tests |
|---|---|---|---|---|
| Controller | Endpoint HTTP + délégation | N/A | | |
| Use case / Service | Orchestration métier | | | |
| Domain | Règles métier / invariants | | | |
| Infrastructure | Persistence / clients externes | | | |

### Couche front/mobile

| Élément | Responsabilité | Ce qui est interdit |
|---|---|---|
| Page / Screen | Orchestration UI | Règle métier critique |
| Component / Widget | Affichage réutilisable | Logique complexe |
| Service / Facade / Provider | Appels API + état présentation | Décision métier source de vérité |

### Vérifications

- [ ] Le backend est maître de la règle métier.
- [ ] Le frontend/mobile ne contient pas de logique métier critique.
- [ ] Les controllers ne contiennent aucune logique métier.
- [ ] Les services/use cases sont clairement séparés.
- [ ] Les abstractions sont justifiées.
- [ ] Les limites de taille sont respectées.
