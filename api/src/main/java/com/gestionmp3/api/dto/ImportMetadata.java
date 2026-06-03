package com.gestionmp3.api.dto;

/**
 * Partie JSON "metadata" recue lors de l'import depuis le back office (Envoyeur).
 * Les noms de champs correspondent exactement au record {@code Mp3Metadata} cote
 * back office pour une deserialisation directe.
 */
public record ImportMetadata(
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
