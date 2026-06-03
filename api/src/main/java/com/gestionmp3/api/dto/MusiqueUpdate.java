package com.gestionmp3.api.dto;

/** Champs modifiables d'une musique via le CRUD web (PUT). */
public record MusiqueUpdate(
        String titre,
        String artiste,
        String album,
        String genre,
        String langue,
        String annee) {
}
