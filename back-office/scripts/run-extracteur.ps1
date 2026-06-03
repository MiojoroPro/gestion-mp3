# Programme 2 : extrait les metadonnees des MP3 (consumer long, Ctrl+C pour arreter).
$jar = "$PSScriptRoot\..\extracteur\target\extracteur.jar"

if (-not (Test-Path $jar)) {
    Write-Error "Jar introuvable : $jar. Lance d'abord 'mvn clean package' dans back-office."
    exit 1
}

Write-Host "Extracteur en attente de messages... (Ctrl+C pour arreter)" -ForegroundColor Cyan
java -jar $jar
