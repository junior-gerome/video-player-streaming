# Modèles de prompts — kit Dev

> Fichier destiné aux **humains** : modèles de demandes prêts à copier dans l'assistant IA.
> L'IA ne lit pas ce fichier.

## Mode d'emploi

1. Copier le bloc du modèle voulu.
2. Remplacer **chaque** élément `{{…}}` (accolades comprises) par votre valeur.
   `{{a | b | c}}` signifie : garder une seule des valeurs proposées.
3. Ne pas modifier les **mots-clés** en majuscules suivis de `:` (`MODE :`, `GABARIT :`…).
4. Supprimer une ligne si l'information n'existe pas (ex. pas d'erreur), sauf `MODE`.

Si un `{{…}}` est oublié, l'IA demandera la valeur au lieu de l'inventer.

## Sommaire

- D01 — Corriger un bug simple (gabarit LÉGER)
- D02 — Comprendre un bug avant de coder
- D03 — Nouvelle fonctionnalité backend (gabarit STANDARD)
- D04 — Nouvel écran Angular (gabarit STANDARD)
- D05 — Nouvel écran Flutter (gabarit STANDARD)
- D06 — Refactoring sans changement de comportement
- D07 — Mettre à jour une dépendance
- D08 — Relire une merge request
- D09 — Découper et estimer un besoin
- D10 — Planifier un sprint
- D11 — Rapport hebdomadaire ou fin de sprint
- D12 — Prendre une décision d'architecture
- D13 — Préparer une release
- D14 — Hotfix en production
- D15 — Trier les écarts aux standards
- D16 — Cartographier l'existant
- D17 — Reprendre le backlog existant
- D18 — Créer une entité CRUD complète (backend)
- D19 — Créer les écrans Angular d'une entité
- D20 — Monter un projet existant au socle de référence

## Développement

### D01 — Corriger un bug simple (gabarit LÉGER)

*Quand :* Correctif < 0,5 jour, sans changement d'API, de base ou d'authentification.

```text
MODE : Engineering {{backend | Angular | Flutter}}
GABARIT : LÉGER
TICKET : {{ID_TICKET}}
BUG : {{SYMPTOME_OBSERVE}}
ERREUR : {{PREMIERE_EXCEPTION_ET_CAUSED_BY | aucune}}
ATTENDU : {{COMPORTEMENT_ATTENDU}}
CONSIGNES : cherche avec rg avant d'ouvrir les fichiers ; ajoute un test ; réponse au format court.
```

### D02 — Comprendre un bug avant de coder

*Quand :* Cause inconnue, incident, comportement incompris.

```text
MODE : Diagnostic
TICKET : {{ID_TICKET}}
SYMPTOME : {{CE_QUI_SE_PASSE}}
REPRODUCTION : {{ETAPES_POUR_REPRODUIRE}}
ERREUR : {{EXTRAIT_DE_LOG_OU_EXCEPTION}}
ENVIRONNEMENT : {{dev | recette | production}}
CONSIGNES : ne code rien ; donne la cause probable, les preuves et le plan de correction dans le ticket.
```

### D03 — Nouvelle fonctionnalité backend (gabarit STANDARD)

*Quand :* Nouvel endpoint, nouvelle règle métier, migration de base.

```text
MODE : Engineering backend
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
FEATURE : {{OBJECTIF_METIER_EN_UNE_PHRASE}}
CRITERES :
- {{CRITERE_1}}
- {{CRITERE_2}}
- {{CRITERE_3}}
API : {{METHODE_ET_CHEMIN | a proposer}}
CONSIGNES : crée la doc feature (1 page chacune), implémente avec tests, lance scripts\check-standards.bat, réponse au format court.
```

### D04 — Nouvel écran Angular (gabarit STANDARD)

*Quand :* Nouvel écran ou composant significatif.

```text
MODE : Engineering Angular
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
FEATURE : {{OBJECTIF_DE_L_ECRAN}}
CRITERES :
- {{CRITERE_1}}
- {{CRITERE_2}}
API UTILISEE : {{ENDPOINTS_BACKEND}}
CONSIGNES : lis DESIGN.md ; Tailwind v4, light/dark, textes en i18n fr/en, composants réutilisables ; tests ; réponse au format court.
```

### D05 — Nouvel écran Flutter (gabarit STANDARD)

*Quand :* Nouvel écran mobile.

```text
MODE : Engineering Flutter
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
FEATURE : {{OBJECTIF_DE_L_ECRAN}}
CRITERES :
- {{CRITERE_1}}
- {{CRITERE_2}}
CONSIGNES : lis DESIGN.md ; ThemeData light/dark, AppLocalizations fr/en, widgets réutilisables ; flutter analyze et flutter test ; réponse au format court.
```

### D06 — Refactoring sans changement de comportement

*Quand :* Classe trop longue, duplication, dette technique.

```text
MODE : Engineering {{backend | Angular | Flutter}}
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
CIBLE : {{CHEMIN_DE_LA_CLASSE_OU_DU_MODULE}}
OBJECTIF : {{EX. DECOUPER_EN_SERVICES_DE_MOINS_DE_300_LIGNES}}
CONSIGNES : écris d'abord des tests qui figent le comportement actuel ; aucun changement fonctionnel ; réponse au format court.
```

### D07 — Mettre à jour une dépendance

*Quand :* Montée de version Spring Boot, Angular, librairie.

```text
MODE : Engineering {{backend | Angular | Flutter}}
GABARIT : {{LÉGER | STANDARD}}
TICKET : {{ID_TICKET}}
DEPENDANCE : {{NOM}} de {{VERSION_ACTUELLE}} vers {{VERSION_CIBLE}}
CONSIGNES : liste les changements cassants connus, adapte le code, lance tous les tests, indique l'impact SemVer.
```

## Qualité

### D08 — Relire une merge request

*Quand :* Avant chaque merge.

```text
MODE : QA Review
TICKET : {{ID_TICKET}}
BRANCHE : {{NOM_DE_LA_BRANCHE}}
BASE : {{main | develop}}
CONSIGNES : analyse uniquement le diff (git diff {{BASE}}...{{NOM_DE_LA_BRANCHE}}) ; n'ouvre que les fiches de revue concernées ; rapport P0 à P3 avec verdict.
```

## Pilotage

### D09 — Découper et estimer un besoin

*Quand :* Besoin macro ou flou, plus de 2 à 3 jours.

```text
MODE : Project Manager
BESOIN : {{DESCRIPTION_DU_BESOIN}}
EQUIPE : {{EX. 1 SENIOR, 2 INTERMEDIAIRES, 1 JUNIOR}}
SPRINT : {{DUREE_EN_SEMAINES}} semaines, focus {{POURCENTAGE}} %
CONSIGNES : découpe en EPIC > user stories > tasks, estime en story points et par profil, propose le contenu du prochain sprint ; ne code rien.
```

### D10 — Planifier un sprint

*Quand :* Avant chaque sprint.

```text
MODE : Project Manager
SPRINT : {{NUMERO_DU_SPRINT}} du {{DATE_DEBUT}} au {{DATE_FIN}}
CAPACITE : {{ABSENCES_ET_DISPONIBILITES}}
PRIORITES : {{TICKETS_OU_OBJECTIFS_PRIORITAIRES}}
CONSIGNES : crée docs/pm/sprints/SPRINT-{{NUMERO_DU_SPRINT}}.md depuis le template, compare charge et capacité, signale les risques.
```

### D11 — Rapport hebdomadaire ou fin de sprint

*Quand :* Chaque semaine ou en fin de sprint.

```text
MODE : Project Manager
PERIODE : {{SEMAINE_OU_SPRINT}}
CONSIGNES : rapport depuis docs/ai/PROJECT-TRACKING.md (réalisé, dérives, blocages, risques) ; en fin de sprint, archive les lignes DONE dans docs/ai/archive/TRACKING-{{SPRINT}}.md.
```

### D12 — Prendre une décision d'architecture

*Quand :* Choix de technologie, découpage, migration majeure, exception à un standard.

```text
MODE : Architecture
QUESTION : {{DECISION_A_PRENDRE}}
OPTIONS : {{OPTION_A}} ; {{OPTION_B}}
CONTRAINTES : {{DELAIS_BUDGET_EXISTANT}}
CONSIGNES : compare les options, recommande, rédige docs/ai/adr/ADR-{{NUMERO}}-{{SUJET}}.md.
```

## Livraison

### D13 — Préparer une release

*Quand :* Fin de sprint, livraison planifiée.

```text
MODE : Release
DEPUIS : {{DERNIERE_VERSION_LIVREE}}
CONSIGNES : analyse les tickets DONE, propose le bump SemVer, mets à jour VERSION et CHANGELOG, crée la release note, propose le tag Git sans le pousser.
```

### D14 — Hotfix en production

*Quand :* Bug critique en production.

```text
MODE : Release
TYPE : hotfix
DEPUIS LE TAG : {{TAG_EN_PRODUCTION}}
BUG : {{SYMPTOME_EN_PRODUCTION}}
ERREUR : {{EXTRAIT_DE_LOG}}
CONSIGNES : correctif minimal uniquement, test de non-régression, bump PATCH, changelog, tag proposé ; rappelle de reporter la correction sur la branche principale.
```

## Reprise de projet

### D15 — Trier les écarts aux standards

*Quand :* Projet existant, juste après l'installation du kit.

```text
MODE : Architecture
CONTEXTE : projet en production depuis {{DUREE}}, budget de refonte {{faible | moyen | fort}}
ECARTS :
{{COLLER_LA_SORTIE_DE_check-standards.bat}}
CONSIGNES : pour chaque écart propose corriger / migrer / exception avec justification ; rédige les ADR d'exception ; crée les tickets de mise en conformité avec gabarit et priorité.
```

### D16 — Cartographier l'existant

*Quand :* Projet repris sans documentation.

```text
MODE : Diagnostic
OBJECTIF : produire docs/ARCHITECTURE-EXISTANT.md (2 pages maximum)
CONTENU : modules, points d'entrée, base de données, intégrations externes, zones sans tests, dette visible
CONSIGNES : travaille par recherche (rg, arborescence, pom.xml, routes) ; n'ouvre pas les fichiers un par un ; signale ce qui n'a pas pu être vérifié.
```

### D17 — Reprendre le backlog existant

*Quand :* Import des tickets depuis Jira, GitLab ou Excel.

```text
MODE : Project Manager
SOURCE : {{CHEMIN_DE_L_EXPORT_CSV}}
CONSIGNES : remplis docs/ai/PROJECT-TRACKING.md avec les tickets ouverts uniquement, classe par priorité, signale les tickets de plus de 3 jours à redécouper, ajoute les tickets de mise en conformité issus des ADR.
```

## Socle et entités

### D18 — Créer une entité CRUD complète (backend)

*Quand :* Nouvelle entité gérée en création, lecture, modification, suppression.

```text
MODE : Engineering backend
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
PREFIXE : {{PREFIXE_PROJET}}
ENTITE : {{NOM_ENTITE}}
MODULE : {{NOM_DU_MODULE_DE_DOMAINE}}
CHAMPS : {{CHAMP_TYPE_CONTRAINTE, ...}}
RELATIONS : {{RELATIONS | aucune}}
CONSIGNES : entité IEntity (Lombok @Getter/@Setter), BasicDTO et FullDTO (@Data + @SuperBuilder), repository, service étendant IAbstractService, contrôleur héritant d'AbstractController ; codes d'erreur dans docs/ERROR-CODES.md ; tests du service ; réponse au format court.
```

### D19 — Créer les écrans Angular d'une entité

*Quand :* Liste et formulaire pour une entité déjà exposée par le backend.

```text
MODE : Engineering Angular
GABARIT : STANDARD
TICKET : {{ID_TICKET}}
PREFIXE : {{PREFIXE_PROJET}}
ENTITE : {{NOM_ENTITE}}
DOMAINE : {{DOSSIER_FEATURES}}
ENDPOINT : /api/v1/{{RESSOURCE}}
PAGINATION : {{client | serveur}}
CONSIGNES : modèles Basic/Full alignés sur les DTO, service étendant CrudService, écran de liste et formulaire création/modification, clés i18n fr/en, squelettes de chargement ; réponse au format court.
```

### D20 — Monter un projet existant au socle de référence

*Quand :* Projet en Java 17, Spring Boot 3.x ou Angular antérieur à 20.

```text
MODE : Architecture
CONTEXTE : versions actuelles : Java {{VERSION_JAVA}}, Spring Boot {{VERSION_SPRING_BOOT}}, Angular {{VERSION_ANGULAR}}
CIBLE : Java 21, Spring Boot 4.1.x, Angular 20, Lombok selon le standard
CONSIGNES : liste les changements cassants (Jakarta EE 11, Spring Security 7, Jackson 3, contrôle de flux Angular, zoneless), propose un découpage en tickets ordonnés avec estimation, rédige l'ADR de migration ; ne modifie aucun code.
```
