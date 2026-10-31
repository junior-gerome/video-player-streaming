# Catalogue des codes d'erreur — {PROJET}

> Source unique des codes renvoyés dans `ApiResponse.errorCode`.
> Chaque ligne a sa traduction `errors.<code>` dans `public/i18n/fr.json` et `en.json`.
> Format : `{PREFIXE}-{CATEGORIE}-{NUMERO}` ; catégories : `VAL`, `MET`, `SEC`, `TEC`.

| Code | Catégorie | HTTP | Exception | Signification | Clé i18n front |
|---|---|---:|---|---|---|
| {PREFIXE}-VAL-001 | Validation | 400 | `IllegalArgumentException` | Paramètre manquant ou invalide | `errors.{PREFIXE}-VAL-001` |
| {PREFIXE}-MET-001 | Métier | 422 | `BusinessException` | Action interdite dans l'état actuel de la ressource | `errors.{PREFIXE}-MET-001` |
| {PREFIXE}-MET-002 | Métier | 404 | `ResourceNotFoundException` | Ressource introuvable | `errors.{PREFIXE}-MET-002` |
| {PREFIXE}-SEC-001 | Sécurité | 403 | `AccessDeniedException` | Droits insuffisants | `errors.{PREFIXE}-SEC-001` |
| {PREFIXE}-SEC-002 | Sécurité | 401 | `UnauthorizedException` | Authentification absente ou invalide | `errors.{PREFIXE}-SEC-002` |
| {PREFIXE}-TEC-001 | Technique | 500 | `TechnicalException` | Erreur interne | `errors.{PREFIXE}-TEC-001` |

Règles : un code n'est jamais réutilisé pour un autre sens ; un code retiré
reste listé avec la mention « obsolète ».
