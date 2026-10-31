# Audit de consolidation — kit 0.5.0

Entrées : `skill__2_.md` (backend) et `angular-skill.md` (frontend) fournis par
l'équipe ; kit 0.4.1. Consigne : Java 21, versions supportées, Angular 20,
suppression des doublons, ajout de Lombok.

## Versions retenues

| Élément | Retenu | Motif |
|---|---|---|
| Java | 21 LTS | consigne |
| Spring Boot | 4.1.x | toute la gamme 3.x est hors support gratuit depuis le 30/06/2026 ; 4.1 est la branche supportée la plus longue |
| Angular | 20 | consigne (support long terme jusqu'au 21/11/2026) |
| Node.js | 22 LTS | compatible Angular 20 (`^20.19 || ^22.12 || >=24`) |
| Lombok | 1.18.46 (via BOM Spring Boot) | support JDK 21 à 26 |
| SpringDoc | 3.x | première gamme compatible Spring Boot 4 |

## Incohérences corrigées

| Constat | Correction |
|---|---|
| Le backend enveloppe tout dans `ApiResponse<T>` ; le `CrudService` Angular lisait directement `B[]` | `CrudService` déballe `ApiResponse.data` |
| Le front lisait `errorCode`, absent de l'`ApiResponse` backend | `errorCode` obligatoire dans toute erreur ; catalogue `docs/ERROR-CODES.md` |
| `@defer` présenté comme gestion du chargement de données | `@defer` réservé au chargement différé de code ; données : squelette sur `isLoading()` |
| `AuthService` déclaré en interface (inutilisable comme jeton d'injection) ; `hasRole` absent du contrat | classe abstraite avec `hasRole` |
| `CrudService` `@Injectable({providedIn:'root'})` avec un paramètre `string` non injectable | classe abstraite, `HttpClient` par `inject()` |
| Intercepteur décrit comme classe, codé en fonction | fonction `authInterceptor` |
| Messages d'erreur et libellés codés en dur en français | clés i18n `errors.*` en `fr` et `en` |
| Classes `rounded-2xl`, `rounded-full`, couleurs en dur, Bootstrap proposé | tokens Tailwind v4, arrondis ≤ 8 px, Tailwind uniquement |
| `deleteById(int)` / `findById(int)` contre `Integer` dans `IEntity` | `Integer` partout |
| Numérotation cassée (section 7 contenant « 6.1 »), renvois « section n » | titres renumérotés, renvois par nom |
| Kit : généricité CRUD interdite, `ServiceImpl` déconseillé, domaine sans JPA, DTO immuables | alignés sur le standard d'équipe |
| Kit : fiches de revue en anglais citant Spring Boot 4.0, Angular 22, RFC 9457, `record` obligatoire | réécrites en français et alignées |
| Kit : `--radius-md` à 12 px dans le thème, contraire à la règle des 8 px | 6 px ; `lg` et `xl` à 8 px |
| Kit : `app.config.ts` utilisé pour le branding, en conflit avec le fichier de providers Angular | branding dans `configurations/app-settings.ts` |
| Kit : Lombok absent | section dédiée, règle S12, contrôles automatiques, `lombok.config` |

## Doublons supprimés

- Listes de contrôle des deux skills : déplacées dans `docs/ai/review/`.
- Arborescences Angular (3 versions différentes) : une seule, dans le standard Angular.
- Arborescences Spring (2 versions contradictoires) : une seule, dans le standard backend.
- Règles thème / i18n Angular : uniquement dans le standard Angular.
- Code d'exemple Angular : dans `docs/templates/angular/`, lu seulement à la création.

## Vérifications effectuées

- Modèles Angular compilés avec Angular 20.3 et TypeScript 5.8, `strictTemplates`.
- Thème compilé avec Tailwind CSS 4.3 : classes du standard générées.
- `pom-java21-lombok.xml.example` : XML valide ; `application.yml.example` : YAML valide.
- `check-standards.bat` testé sur un projet conforme (0 FAIL) et non conforme (9 FAIL).
- Non vérifié : compilation Java (dépôt Maven inaccessible depuis l'environnement d'audit).
