package com.gestionmp3.api.web;

/** Levee quand une ressource demandee n'existe pas (renvoie un HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
