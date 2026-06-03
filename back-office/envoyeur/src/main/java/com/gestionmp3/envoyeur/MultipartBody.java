package com.gestionmp3.envoyeur;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Construit un corps {@code multipart/form-data} a la main pour l'envoyer avec
 * le {@link java.net.http.HttpClient} du JDK (qui ne sait pas le faire seul).
 * Le corps melange du texte (JSON) et du binaire (le MP3), d'ou la manipulation
 * directe d'octets.
 */
final class MultipartBody {

    private final String boundary = "----GestionMp3Boundary" + System.nanoTime();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /** Ajoute un champ texte (ici utilise pour la partie JSON des metadonnees). */
    MultipartBody addTextPart(String name, String contentType, String value) throws IOException {
        writeAscii("--" + boundary + "\r\n");
        writeAscii("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
        writeAscii("Content-Type: " + contentType + "\r\n\r\n");
        buffer.write(value.getBytes(StandardCharsets.UTF_8));
        writeAscii("\r\n");
        return this;
    }

    /** Ajoute un fichier binaire (le contenu du .mp3). */
    MultipartBody addFilePart(String name, Path file) throws IOException {
        String fileName = file.getFileName().toString();
        writeAscii("--" + boundary + "\r\n");
        writeAscii("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n");
        writeAscii("Content-Type: audio/mpeg\r\n\r\n");
        buffer.write(Files.readAllBytes(file));
        writeAscii("\r\n");
        return this;
    }

    /** Cloture le corps multipart et renvoie les octets prets a etre envoyes. */
    byte[] build() throws IOException {
        writeAscii("--" + boundary + "--\r\n");
        return buffer.toByteArray();
    }

    String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    private void writeAscii(String text) throws IOException {
        buffer.write(text.getBytes(StandardCharsets.US_ASCII));
    }
}
