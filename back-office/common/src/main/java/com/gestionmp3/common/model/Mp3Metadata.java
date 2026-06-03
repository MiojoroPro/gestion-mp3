package com.gestionmp3.common.model;

/**
 * Message emis par l'Extracteur : les metadonnees extraites d'un fichier .mp3.
 * Reprend le chemin/identite du fichier (pour que l'Envoyeur puisse lire le
 * binaire et que le Suppresseur sache quoi effacer ensuite).
 *
 * @param path            chemin absolu du fichier sur le disque
 * @param fileName        nom du fichier
 * @param sizeBytes       taille en octets
 * @param title           titre du morceau
 * @param artist          artiste / interprete
 * @param album           album
 * @param genre           genre musical
 * @param language        langue
 * @param year            annee de sortie
 * @param durationSeconds duree en secondes
 * @param bitrate         debit en kbps (tel que rapporte par la lib)
 * @param trackNumber     numero de piste
 */
public record Mp3Metadata(
        String path,
        String fileName,
        long sizeBytes,
        String title,
        String artist,
        String album,
        String genre,
        String language,
        String year,
        int durationSeconds,
        String bitrate,
        String trackNumber) {
}
