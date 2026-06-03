# Programme 3 : envoie les MP3 + metadonnees a l'API web (consumer long, Ctrl+C pour arreter).
# Usage : .\run-envoyeur.ps1 [url-api]
param(
    [string]$ApiUrl = "http://localhost:8080/api/musiques/import"
)

$env:API_URL = $ApiUrl
$jar = "$PSScriptRoot\..\envoyeur\target\envoyeur.jar"

if (-not (Test-Path $jar)) {
    Write-Error "Jar introuvable : $jar. Lance d'abord 'mvn clean package' dans back-office."
    exit 1
}

Write-Host "Envoyeur -> API : $ApiUrl (Ctrl+C pour arreter)" -ForegroundColor Cyan
java -jar $jar
