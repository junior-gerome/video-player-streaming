# Installation — kit v0.5.0 (Windows)

Toutes les commandes sont données pour **PowerShell** (ou cmd lorsque c'est
indiqué). Les scripts `.bat` ne demandent aucune installation : ils utilisent
Windows PowerShell, intégré à Windows 10/11.

## 1. Nouveau projet

```powershell
$KIT = "C:\Outils\integration-kit-v0.5.0"
cd C:\Dev\mon-projet
Copy-Item "$KIT\AGENTS.md","$KIT\CLAUDE.md","$KIT\GEMINI.md","$KIT\SKILL.md","$KIT\PROJECT-MANAGER-SKILL.md" .
Copy-Item "$KIT\scripts","$KIT\docs" . -Recurse
Copy-Item "$KIT\docs\templates\gitignore\.gitignore.spring-angular-flutter.example" .gitignore
Set-Content VERSION "0.1.0"
```

1. Choisir le `.gitignore` adapté à la stack dans `docs\templates\gitignore\`.
2. `VERSION` contient la version **du projet** (0.1.0 au départ), pas celle du kit.
3. Brancher le contrôle en CI : `docs\templates\ci\`.
4. Si UI : créer `DESIGN.md` depuis `docs\templates\design\DESIGN.md.example`.
5. Backend : copier `docs\templates\spring\lombok.config.example` en `lombok.config`
   à la racine du backend et partir de `pom-java21-lombok.xml.example` pour le `pom.xml` parent ;
   créer `docs\ERROR-CODES.md` depuis `docs\templates\documentation\TEMPLATE-ERROR-CODES.md`.
6. Vérifier : `scripts\check-standards.bat .` doit afficher `0 FAIL`.
7. Committer : `git add -A` puis `git commit -m "chore: add AI governance kit v0.5.0"`.

Le dossier `.kit\` n'est pas à copier dans un projet (historique du kit).

## 2. Projet existant (sans kit)

```powershell
$KIT = "C:\Outils\integration-kit-v0.5.0"
cd C:\Dev\projet-existant
git checkout -b chore/ai-kit
foreach ($f in "AGENTS.md","SKILL.md","PROJECT-MANAGER-SKILL.md","GEMINI.md","CLAUDE.md") {
  if (Test-Path $f) { Write-Host "$f existe déjà : fusion manuelle nécessaire" }
  else { Copy-Item "$KIT\$f" . }
}
# robocopy /XC /XN /XO : copie uniquement les fichiers absents, n'écrase rien
robocopy "$KIT\scripts" scripts /E /XC /XN /XO /NJH /NJS
robocopy "$KIT\docs" docs /E /XC /XN /XO /NJH /NJS
Set-Content VERSION "{{VERSION_EN_PRODUCTION}}"
scripts\check-standards.bat .
```

- Ne jamais écraser un `.gitignore`, un `README.md` ou un `CLAUDE.md` existant :
  les fusionner à la main (pour `CLAUDE.md`, ajouter `@AGENTS.md` en première ligne).
- `VERSION` reprend la version réellement en production.
- Traiter la sortie de `check-standards.bat` : corriger, migrer ou déclarer une
  exception ADR (modèle D15 de `docs\prompts\PROMPTS-DEV.md`).
- Activer la CI d'abord en mode non bloquant (`allow_failure: true`), puis bloquant.

## 3. Migration depuis 0.3.x ou 0.4.x

1. **Sauvegarder** vos fichiers projet : `docs\ai\PROJECT-TRACKING.md`,
   `docs\ai\CHANGELOG.md`, `docs\ai\tickets\TICKET-*`, `docs\ai\adr\ADR-*`,
   `docs\pm\sprints\`, `docs\pm\reports\`, `docs\features\`, `DESIGN.md`, `VERSION`.
2. Remplacer les fichiers du kit : racine (`AGENTS.md`, `CLAUDE.md`,
   `GEMINI.md`, `SKILL.md`, `PROJECT-MANAGER-SKILL.md`), `scripts\`,
   `docs\standards\`, `docs\templates\`, `docs\qa\`, `docs\release\*.md`,
   `docs\pm\*.md`, `docs\pm\templates\`, `docs\ai\README-IA.md`,
   `docs\ai\WORKFLOW-IA.md`, `docs\ai\tickets\TEMPLATE-*`, `docs\ai\review\`,
   et ajouter `docs\prompts\`.
3. Supprimer les anciens fichiers : `scripts\check-standards.sh`,
   `scripts\kit-budget.py`, `docs\ai\review-checklist.md` (depuis 0.3.x),
   `docs\standards\FRONTEND-MOBILE-STANDARDS.md` (remplacé par
   `FRONTEND-ANGULAR-STANDARDS.md` et `MOBILE-FLUTTER-STANDARDS.md`),
   `docs\templates\spring\ARCHITECTURE-PACKAGE-STRUCTURE.md`,
   `docs\templates\angular\app.config.ts.example` dans son ancienne forme
   (le branding passe dans `app-settings.ts`).
4. Restaurer vos fichiers projet sauvegardés.
5. Depuis 0.3.x : retirer du `CHANGELOG.md` l'historique du kit (versions
   0.1.0 à 0.3.8 du kit) et déplacer les lignes DONE de `PROJECT-TRACKING.md`
   dans `docs\ai\archive\TRACKING-{{SPRINT}}.md`.
6. Depuis 0.4.x : ajouter `lombok.config` et `docs\ERROR-CODES.md` (voir
   « Nouveau projet ») ; dans un projet Angular, déplacer le branding de
   `app.config.ts` vers `configurations\app-settings.ts`.
7. Lancer `scripts\check-standards.bat .`. Les nouvelles règles (Java 21,
   Spring Boot 4.1, Angular 20, S12 Lombok, S13 Angular) peuvent produire des
   FAIL : les traiter avec le modèle D20 (montée de version) ou D15 (tri).

## 4. Exceptions aux standards

Une exception validée par ADR se déclare dans `.standards-exceptions` à la
racine du projet, une ligne par règle :

```text
S3 ADR-0004 Angular Material conservé sur le module legacy jusqu'à la v3
```

La règle passe alors de FAIL à WARN dans `check-standards.bat`.

## 5. Outils IA

| Outil | Fichier lu automatiquement | Remarque |
|---|---|---|
| Claude Code | `CLAUDE.md` → `@AGENTS.md` | `/clear` entre tâches, `/compact` sur tâche longue, `/context` pour contrôler |
| Gemini CLI | `GEMINI.md` → `@./AGENTS.md` | ne pas rajouter d'imports `@` |
| Codex / autres | `AGENTS.md` | coller un modèle de `docs\prompts\PROMPTS-DEV.md` |
| Copilot | instructions du dépôt | pointer vers `AGENTS.md` |

## 6. Maintenance du kit

- Ajouter une règle **à un seul endroit** (standard de domaine) ; si elle est
  non négociable, une ligne dans `AGENTS.md` (section « Standards non
  négociables ») et, si possible, un contrôle dans `check-standards.bat`.
- Avant chaque version du kit : `scripts\kit-budget.bat --max-core 3000`.
