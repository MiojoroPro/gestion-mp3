# Programme 4 : supprime les .mp3 deja traites (consumer long, Ctrl+C pour arreter).
$jar = "$PSScriptRoot\..\suppresseur\target\suppresseur.jar"

if (-not (Test-Path $jar)) {
    Write-Error "Jar introuvable : $jar. Lance d'abord 'mvn clean package' dans back-office."
    exit 1
}

Write-Host "Suppresseur en attente de messages... (Ctrl+C pour arreter)" -ForegroundColor Cyan
java -jar $jar
