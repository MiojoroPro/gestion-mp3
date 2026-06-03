# Programme 1 : scanne le dossier MP3 et publie les fichiers decouverts.
# Usage : .\run-lister.ps1 [chemin-du-dossier-mp3]
param(
    [string]$InboxDir = "$PSScriptRoot\..\..\mp3-inbox"
)

$env:MP3_INBOX_DIR = $InboxDir
$jar = "$PSScriptRoot\..\lister\target\lister.jar"

if (-not (Test-Path $jar)) {
    Write-Error "Jar introuvable : $jar. Lance d'abord 'mvn clean package' dans back-office."
    exit 1
}

Write-Host "Lister -> dossier : $InboxDir" -ForegroundColor Cyan
java -jar $jar
