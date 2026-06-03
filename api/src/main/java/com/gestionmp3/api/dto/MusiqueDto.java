package com.gestionmp3.api.dto;

import com.gestionmp3.api.model.Musique;

import java.time.Instant;

/** Representation d'une musique renvoyee par l'API (sans exposer le chemin disque). */
public record MusiqueDto(
        Long id,
        String titre,
        String artiste,
        String album,
        String genre,
        String langue,
        String annee,
        int dureeSecondes,
        String bitrate,
        String numeroPiste,
        String nomFichier,
        long tailleOctets,
        Instant dateAjout) {

    public static MusiqueDto from(Musique m) {
        return new MusiqueDto(
                m.getId(),
                m.getTitre(),
                m.getArtiste(),
                m.getAlbum(),
                m.getGenre(),
                m.getLangue(),
                m.getAnnee(),
                m.getDureeSecondes(),
                m.getBitrate(),
                m.getNumeroPiste(),
                m.getNomFichier(),
                m.getTailleOctets(),
                m.getDateAjout());
    }
}
