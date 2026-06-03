# Back Office — Gestion MP3

Chaîne de traitement des fichiers MP3 par **messages** (RabbitMQ). Quatre programmes
Java indépendants, chacun responsable d'une étape, reliés par des files d'attente.

```
┌──────────┐  mp3.discovered  ┌─────────────┐  mp3.metadata  ┌───────────┐  mp3.sent  ┌──────────────┐
│  Lister  │ ───────────────▶ │  Extracteur │ ─────────────▶ │  Envoyeur │ ─────────▶ │ Suppresseur  │
└──────────┘                  └─────────────┘                └───────────┘            └──────────────┘
  scanne le dossier            extrait les tags ID3            POST multipart           supprime le
  publie 1 msg/fichier         (jaudiotagger)                  vers l'API web           .mp3 traité
```

Chaque programme **journalise** son activité (horodatage, succès, erreurs) à la fois
dans la console et dans `logs/<programme>.log`.

## Modules Maven

| Module        | Rôle                                                              | Main class                              |
|---------------|------------------------------------------------------------------|-----------------------------------------|
| `common`      | Connexion RabbitMQ, topologie, modèles de messages, config, JSON | —                                       |
| `lister`      | Programme 1 : liste les `.mp3` du dossier                        | `com.gestionmp3.lister.ListerApp`       |
| `extracteur`  | Programme 2 : extrait les métadonnées                            | `com.gestionmp3.extracteur.ExtracteurApp` |
| `envoyeur`    | Programme 3 : envoie fichier + métadonnées à l'API              | `com.gestionmp3.envoyeur.EnvoyeurApp`   |
| `suppresseur` | Programme 4 : supprime les `.mp3` traités                       | `com.gestionmp3.suppresseur.SuppresseurApp` |

## Files & messages RabbitMQ

Exchange direct unique : `mp3.exchange`. Trois files durables :

| File              | Producteur  | Consommateur  | Charge utile (JSON)                       |
|-------------------|-------------|---------------|-------------------------------------------|
| `mp3.discovered`  | Lister      | Extracteur    | `Mp3File` (chemin, nom, taille)           |
| `mp3.metadata`    | Extracteur  | Envoyeur      | `Mp3Metadata` (titre, artiste, durée, …)  |
| `mp3.sent`        | Envoyeur    | Suppresseur   | `Mp3File` (fichier envoyé avec succès)    |

Garanties : messages persistants, accusés de réception **manuels** (`basicAck`). En
cas d'erreur, le message est rejeté sans suppression du fichier (`basicNack`, pas de
remise en file pour éviter les boucles sur un fichier corrompu).

## Configuration (variables d'environnement)

| Variable        | Défaut                                        | Utilisé par |
|-----------------|-----------------------------------------------|-------------|
| `RABBIT_HOST`   | `localhost`                                   | tous        |
| `RABBIT_PORT`   | `5672`                                         | tous        |
| `RABBIT_USER`   | `guest`                                        | tous        |
| `RABBIT_PASS`   | `guest`                                        | tous        |
| `MP3_INBOX_DIR` | `./mp3-inbox`                                  | Lister      |
| `API_URL`       | `http://localhost:8080/api/musiques/import`    | Envoyeur    |

## Démarrage

### 1. Infrastructure (depuis la racine du projet)

```powershell
docker compose up -d rabbitmq      # console : http://localhost:15672 (guest/guest)
```

### 2. Compiler

```powershell
cd back-office
mvn clean package                  # produit */target/*.jar (fat jars)
```

### 3. Lancer la chaîne

Démarrer les **trois consommateurs** (chacun dans son terminal), puis le Lister :

```powershell
# Terminal A
.\scripts\run-extracteur.ps1
# Terminal B
.\scripts\run-envoyeur.ps1
# Terminal C
.\scripts\run-suppresseur.ps1
# Terminal D — dépose des .mp3 dans mp3-inbox/ puis :
.\scripts\run-lister.ps1
```

> Tant que l'API web n'existe pas, l'Envoyeur recevra une erreur de connexion et
> **conservera** les fichiers (aucune suppression) — c'est le comportement attendu.

## Tester sans l'API

Pour valider Lister → Extracteur seuls, lancer uniquement l'Extracteur puis le Lister :
les métadonnées extraites apparaîtront dans `logs/extracteur.log` et s'accumuleront
dans la file `mp3.metadata` (visible dans la console RabbitMQ).
