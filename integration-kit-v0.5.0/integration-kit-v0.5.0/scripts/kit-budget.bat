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
# kit-budget.bat - estime le cout en tokens de chaque profil de lecture du kit.
#
# Usage   : scripts\kit-budget.bat [--max-core 3000] [--root dossier]
# Estimation : ~3,5 caracteres par token pour du Markdown en francais
# (ordre de grandeur, suffisant pour detecter une derive).
# Code retour 1 si le noyau (AGENTS.md) depasse --max-core.
# ---------------------------------------------------------------------------
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }
$kitArgs = @()
if ($env:KIT_ARGS) {
  foreach ($m in [regex]::Matches($env:KIT_ARGS, '"([^"]*)"|(\S+)')) {
    if ($m.Groups[1].Success) { $kitArgs += $m.Groups[1].Value } else { $kitArgs += $m.Groups[2].Value }
  }
}
$maxCore = 3000
$root = Split-Path -Parent (Split-Path -Parent $env:KIT_SELF)
for ($i = 0; $i -lt $kitArgs.Count; $i++) {
  if ($kitArgs[$i] -eq '--max-core' -and $i + 1 -lt $kitArgs.Count) { $maxCore = [int]$kitArgs[$i + 1]; $i++ }
  elseif ($kitArgs[$i] -eq '--root' -and $i + 1 -lt $kitArgs.Count) { $root = $kitArgs[$i + 1]; $i++ }
}

# Miroir de la table de routage d'AGENTS.md (noyau + fichiers du mode)
$profiles = [ordered]@{
  'Engineering backend'  = @('SKILL.md', 'docs/standards/BACKEND-SPRING-STANDARDS.md')
  'Engineering Angular'  = @('SKILL.md', 'docs/standards/FRONTEND-ANGULAR-STANDARDS.md', 'DESIGN.md')
  'Engineering Flutter'  = @('SKILL.md', 'docs/standards/MOBILE-FLUTTER-STANDARDS.md', 'DESIGN.md')
  'Diagnostic'           = @()
  'QA Review (2 fiches)' = @('docs/qa/QA-REVIEW-WORKFLOW.md', 'docs/ai/review/BLOQUANTS.md', 'docs/ai/review/BACKEND-SPRING.md', 'docs/ai/review/TESTS.md')
  'Project Manager'      = @('PROJECT-MANAGER-SKILL.md', 'docs/pm/ESTIMATION-GUIDE.md')
  'Release'              = @('docs/release/SEMANTIC-VERSIONING.md', 'docs/release/RELEASE-WORKFLOW.md')
}
function Get-Tokens($files) {
  $total = 0
  foreach ($f in $files) {
    $p = Join-Path $root $f
    if (-not (Test-Path -LiteralPath $p) -and $f -eq 'DESIGN.md') { $p = Join-Path $root 'docs/templates/design/DESIGN.md.example' }
    if (Test-Path -LiteralPath $p) { $total += ([IO.File]::ReadAllText($p, [Text.Encoding]::UTF8)).Length }
  }
  return [math]::Round($total / 3.5)
}
$core = Get-Tokens @('AGENTS.md')
Write-Output ('{0,-30} {1,9}' -f 'Profil', '≈ tokens')
Write-Output ('{0,-30} {1,9}' -f 'Noyau (AGENTS.md)', $core)
foreach ($k in $profiles.Keys) { Write-Output ('{0,-30} {1,9}' -f $k, ($core + (Get-Tokens $profiles[$k]))) }
if ($core -gt $maxCore) {
  Write-Output ''
  Write-Output "ÉCHEC : noyau $core > budget $maxCore. Déplacer du détail vers un module."
  exit 1
}
exit 0
