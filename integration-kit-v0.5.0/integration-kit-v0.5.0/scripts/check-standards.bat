<# : Script Windows (partie Batch puis PowerShell) - ne pas modifier ce bloc
@echo off
setlocal
for /f "tokens=2 delims=:." %%c in ('chcp') do set "KIT_OLDCP=%%c"
chcp 65001 >nul
set "KIT_SELF=%~f0"
set "KIT_ARGS=%*"
powershell -NoProfile -ExecutionPolicy Bypass -Command "iex ([IO.File]::ReadAllText($env:KIT_SELF, [Text.Encoding]::UTF8))"
set "KIT_RC=%ERRORLEVEL%"
chcp %KIT_OLDCP% >nul
exit /b %KIT_RC%
#>
# ---------------------------------------------------------------------------
# check-standards.bat - controle automatique des standards du kit (AGENTS.md,
# section "Standards non negociables") : S1 a S4, S6, S8 a S10, S12, S13.
#
# Usage   : scripts\check-standards.bat [dossier_du_projet]
# Exemple : scripts\check-standards.bat .
# Exceptions : fichier .standards-exceptions a la racine du projet, une ligne
#   par regle, par exemple :  S3 ADR-0004 Angular Material conserve (legacy)
#   La regle passe alors de FAIL a WARN.
# Code retour : 0 si aucun FAIL, 1 sinon (utilisable en CI).
# Compatible Windows PowerShell 5.1 et PowerShell 7 (pwsh).
# ---------------------------------------------------------------------------
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }

$kitArgs = @()
if ($env:KIT_ARGS) {
  foreach ($m in [regex]::Matches($env:KIT_ARGS, '"([^"]*)"|(\S+)')) {
    if ($m.Groups[1].Success) { $kitArgs += $m.Groups[1].Value } else { $kitArgs += $m.Groups[2].Value }
  }
}
$root = '.'
if ($kitArgs.Count -gt 0) { $root = $kitArgs[0] }
if (-not (Test-Path -LiteralPath $root -PathType Container)) { Write-Output "Dossier introuvable : $root"; exit 2 }
$base = (Resolve-Path -LiteralPath $root).Path.TrimEnd('\', '/')
Set-Location -LiteralPath $base

$script:fails = 0
$script:warns = 0
$excludedDirs = @('node_modules', 'target', 'build', 'dist', '.dart_tool', '.git', '.angular', 'coverage', '.kit', 'docs', '.gradle', '.idea', '.vscode')
$exceptionsFile = Join-Path $base '.standards-exceptions'

function Test-Excepted([string]$rule) {
  if (-not (Test-Path -LiteralPath $exceptionsFile)) { return $false }
  foreach ($l in [IO.File]::ReadAllLines($exceptionsFile)) { if ($l -match "^$rule(\s|$)") { return $true } }
  return $false
}
function Show-Details($items) { $items | Select-Object -First 3 | ForEach-Object { Write-Output "        $_" } }
function Add-Fail([string]$rule, [string]$msg, $details) {
  if (Test-Excepted $rule) { Write-Output "[WARN] $rule (exception ADR) $msg"; $script:warns++ }
  else { Write-Output "[FAIL] $rule $msg"; $script:fails++ }
  if ($details) { Show-Details $details }
}
function Add-Warn([string]$rule, [string]$msg, $details) {
  Write-Output "[WARN] $rule $msg"; $script:warns++
  if ($details) { Show-Details $details }
}
function Get-Rel([string]$full) { return $full.Substring($base.Length).TrimStart('\', '/') }

# Inventaire unique des fichiers, en sautant les dossiers generes ou volumineux
$all = New-Object System.Collections.Generic.List[string]
$stack = New-Object System.Collections.Stack
$stack.Push($base)
while ($stack.Count -gt 0) {
  $d = $stack.Pop()
  try {
    foreach ($sub in [IO.Directory]::GetDirectories($d)) {
      if ($excludedDirs -notcontains [IO.Path]::GetFileName($sub)) { $stack.Push($sub) }
    }
    foreach ($f in [IO.Directory]::GetFiles($d)) { $all.Add($f) }
  } catch { }
}
function Get-ByName([string[]]$patterns) {
  $res = @()
  foreach ($f in $all) {
    $n = [IO.Path]::GetFileName($f)
    foreach ($p in $patterns) { if ($n -like $p) { $res += $f; break } }
  }
  return $res
}
function Find-Text($files, [string]$regex, [switch]$CaseSensitive) {
  if (-not $files -or @($files).Count -eq 0) { return @() }
  $hits = if ($CaseSensitive) { Select-String -LiteralPath $files -Pattern $regex -CaseSensitive -ErrorAction SilentlyContinue }
          else { Select-String -LiteralPath $files -Pattern $regex -ErrorAction SilentlyContinue }
  return @($hits | ForEach-Object { "$(Get-Rel $_.Path):$($_.LineNumber): $($_.Line.Trim())" })
}

# S1 - Maven uniquement
$g = Get-ByName @('build.gradle', 'build.gradle.kts', 'settings.gradle', 'settings.gradle.kts', 'gradlew', 'gradlew.bat')
if ($g) { Add-Fail 'S1' 'Fichiers Gradle détectés :' ($g | ForEach-Object { Get-Rel $_ }) }

# S2 - application.yml
$p = Get-ByName @('application.properties', 'application-*.properties')
if ($p) { Add-Fail 'S2' 'application*.properties détecté (utiliser application.yml) :' ($p | ForEach-Object { Get-Rel $_ }) }

# S3 / S4 / S6 - seulement si un projet Angular est présent
if (Get-ByName @('angular.json')) {
  $m = Find-Text (Get-ByName @('package.json')) '"@angular/material"'
  if (-not $m) { $m = Find-Text (Get-ByName @('*.ts', '*.html')) '<mat-|Mat[A-Z][A-Za-z]+Module|from .@angular/material' -CaseSensitive }
  if ($m) { Add-Fail 'S3' 'Angular Material détecté :' $m }
  $t = Find-Text (Get-ByName @('*.css', '*.scss')) '@tailwind (base|components|utilities)'
  if ($t) { Add-Fail 'S3' 'Syntaxe Tailwind v3 détectée :' $t }
  $c = Get-ByName @('tailwind.config.js', 'tailwind.config.ts', 'tailwind.config.cjs')
  if ($c) { Add-Fail 'S3' 'tailwind.config.* présent (tokens attendus dans @theme) :' ($c | ForEach-Object { Get-Rel $_ }) }

  if (-not (Get-ByName @('proxy.conf.json', 'proxy.conf.js'))) { Add-Fail 'S4' 'proxy.conf.json absent' $null }
  if (-not (Find-Text (Get-ByName @('angular.json')) '"proxyConfig"')) { Add-Fail 'S4' 'proxyConfig absent de angular.json' $null }
  $tsNoSpec = @(Get-ByName @('*.ts') | Where-Object { $_ -notlike '*.spec.ts' })
  $u = Find-Text $tsNoSpec '[''"`]https?://(localhost|127\.0\.0\.1|\d+\.\d+\.\d+\.\d+)(:\d+)?'
  if ($u) { Add-Fail 'S4' 'URL backend en dur dans le code Angular :' $u }

  # S3 (version) - Angular 20
  foreach ($pkg in (Get-ByName @('package.json'))) {
    $mv = [regex]::Match([IO.File]::ReadAllText($pkg), '"@angular/core"\s*:\s*"[\^~>=\s]*(\d+)')
    if ($mv.Success -and $mv.Groups[1].Value -ne '20') { Add-Fail 'S3' "Angular $($mv.Groups[1].Value) dans $(Get-Rel $pkg) (attendu : 20)" $null }
  }

  # S13 - conventions Angular 20
  $oldFlow = Find-Text (Get-ByName @('*.html')) '\*ng(If|For|Switch)\b' -CaseSensitive
  if ($oldFlow) { Add-Fail 'S13' 'Ancienne syntaxe *ngIf / *ngFor / *ngSwitch (utiliser @if / @for / @switch) :' $oldFlow }
  $inline = @()
  foreach ($f in (Get-ByName @('*.ts') | Where-Object { $_ -notlike '*.spec.ts' })) {
    $txt = [IO.File]::ReadAllText($f)
    if ($txt -match '@Component\s*\(' -and $txt -match '(?m)^\s*(template|styles)\s*:') { $inline += (Get-Rel $f) }
  }
  if ($inline) { Add-Fail 'S13' 'template: ou styles: en ligne dans un composant (utiliser templateUrl) :' $inline }

  $r = Find-Text (Get-ByName @('*.html', '*.ts', '*.css', '*.scss')) 'rounded-(2xl|3xl|full)' -CaseSensitive
  if ($r) { Add-Warn 'S6' 'Arrondis forts à vérifier (autorisés seulement hors cards/inputs/boutons) :' $r }
}

# S1 (versions) - Java 21 et Spring Boot 4.1.x dans les pom.xml
$poms = @(Get-ByName @('pom.xml'))
if ($poms.Count -gt 0) {
  $javaFound = $false
  foreach ($pom in $poms) {
    $txt = [IO.File]::ReadAllText($pom)
    foreach ($m in [regex]::Matches($txt, '<(java\.version|maven\.compiler\.release|maven\.compiler\.source|release)>\s*(\d+)\s*</')) {
      $javaFound = $true
      if ($m.Groups[2].Value -ne '21') { Add-Fail 'S1' "Java $($m.Groups[2].Value) dans $(Get-Rel $pom) (attendu : 21)" $null }
    }
    $sb = [regex]::Match($txt, '<artifactId>spring-boot-(starter-parent|dependencies)</artifactId>\s*<version>\s*([\d.]+)', 'Singleline')
    if ($sb.Success -and -not $sb.Groups[2].Value.StartsWith('4.1.')) {
      Add-Fail 'S1' "Spring Boot $($sb.Groups[2].Value) dans $(Get-Rel $pom) (attendu : 4.1.x)" $null
    }
  }
  if (-not $javaFound) { Add-Warn 'S1' 'Version Java non déclarée dans les pom.xml (attendu : <java.version>21</java.version>)' $null }

  # S12 - Lombok
  $lombokDeclared = $false
  foreach ($pom in $poms) { if ([IO.File]::ReadAllText($pom) -match '<artifactId>lombok</artifactId>') { $lombokDeclared = $true } }
  if (-not $lombokDeclared) { Add-Fail 'S12' 'Lombok absent des pom.xml' $null }
  if (-not (Get-ByName @('lombok.config'))) { Add-Warn 'S12' 'lombok.config absent (modèle : docs\templates\spring\lombok.config.example)' $null }
  $dataOnEntity = @(); $fieldInjection = @(); $forbiddenLombok = @()
  foreach ($f in (Get-ByName @('*.java'))) {
    $lines = [IO.File]::ReadAllLines($f)
    $txt = [string]::Join("`n", $lines)
    if ($txt -match '(?m)^\s*@Entity\b' -and $txt -match '(?m)^\s*@Data\b') { $dataOnEntity += (Get-Rel $f) }
    if ($txt -match '@SneakyThrows|import\s+lombok\.(val|var|Cleanup)\s*;') { $forbiddenLombok += (Get-Rel $f) }
    for ($i = 0; $i -lt $lines.Count; $i++) {
      if ($lines[$i] -match '^\s*@Autowired\b(.*)$') {
        $rest = $matches[1]; $next = ''
        for ($j = $i + 1; $j -lt $lines.Count; $j++) { if ($lines[$j].Trim()) { $next = $lines[$j]; break } }
        if ($rest -match '\b(private|protected|public)\b[^(]*;' -or ($rest.Trim() -eq '' -and $next -match '^\s*(private|protected|public)\b[^(]*;')) {
          $fieldInjection += "$(Get-Rel $f):$($i + 1)"
        }
      }
    }
  }
  if ($dataOnEntity) { Add-Fail 'S12' '@Data sur une entité JPA (utiliser @Getter/@Setter) :' $dataOnEntity }
  if ($fieldInjection) { Add-Fail 'S12' "@Autowired sur un champ (utiliser @RequiredArgsConstructor) :" $fieldInjection }
  if ($forbiddenLombok) { Add-Fail 'S12' 'Fonction Lombok interdite (@SneakyThrows, val, var, @Cleanup) :' $forbiddenLombok }
}

# S8 - taille des fichiers (classes, composants, widgets)
$code = Get-ByName @('*.java', '*.kt', '*.ts', '*.dart') | Where-Object {
  $_ -notmatch '\.spec\.ts$|\.d\.ts$|\.g\.dart$|\.freezed\.dart$|[\\/]generated[\\/]' }
foreach ($f in $code) {
  $n = 0
  try { $n = [IO.File]::ReadAllLines($f).Count } catch { continue }
  if ($n -gt 500) { Add-Fail 'S8' "$(Get-Rel $f) : $n lignes (> 500)" $null }
  elseif ($n -gt 300) { Add-Warn 'S8' "$(Get-Rel $f) : $n lignes (> 300, alerte)" $null }
}

# S9 - secrets probables (avertissement, à confirmer à la main)
$secretFiles = @(Get-ByName @('*.yml', '*.yaml', '*.json', '*.ts', '*.java', '*.dart', '*.env', '*.properties') |
  Where-Object { (Get-Rel $_) -notmatch '(?i)test|spec|package-lock\.json' })
$s = Find-Text $secretFiles '(password|passwd|secret|api[_-]?key|token)["'']?\s*[:=]\s*["'']?[A-Za-z0-9/+_!@#%\-]{6,}' |
  Where-Object { $_ -notmatch '\$\{|process\.env|environment\.|<masqué>|changeme|example' }
if ($s) { Add-Warn 'S9' 'Secrets possibles (vérifier, externaliser) :' $s }

# S10 - .gitignore et fichiers versionnés à tort
if (-not (Test-Path -LiteralPath (Join-Path $base '.gitignore'))) { Add-Fail 'S10' '.gitignore absent à la racine' $null }
if (Get-Command git -ErrorAction SilentlyContinue) {
  $inRepo = (& git rev-parse --is-inside-work-tree 2>$null)
  if ($inRepo -eq 'true') {
    $v = @(& git ls-files 2>$null | Where-Object { $_ -match '(^|/)(node_modules|target|dist|\.dart_tool)/|(^|/)\.env$|\.(jks|p12|pem|key)$' } | Select-Object -First 3)
    if ($v.Count -gt 0) { Add-Fail 'S10' 'Fichiers qui ne devraient pas être versionnés :' $v }
  }
}

Write-Output '---'
Write-Output "check-standards : $($script:fails) FAIL, $($script:warns) WARN"
if ($script:fails -gt 0) { exit 1 } else { exit 0 }
