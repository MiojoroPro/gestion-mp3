package com.gestionmp3.common.model;

/**
 * Message emis par le Lister : un fichier .mp3 decouvert dans le dossier scanne.
 *
 * @param path      chemin absolu du fichier sur le disque
 * @param fileName  nom du fichier (sans le chemin)
 * @param sizeBytes taille du fichier en octets
 */
public record Mp3File(String path, String fileName, long sizeBytes) {
}
