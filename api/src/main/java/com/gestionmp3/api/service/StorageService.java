package com.gestionmp3.api.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Gere le stockage des binaires MP3 sur le disque serveur. Les metadonnees, elles,
 * sont en base ; ici on ne manipule que les fichiers.
 */
@Service
public class StorageService {

    private final Path storageDir;

    public StorageService(@Value("${app.storage.dir}") String dir) {
        this.storageDir = Path.of(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de creer le dossier de stockage : " + storageDir, e);
        }
    }

    /**
     * Enregistre le contenu du fichier importe sous un nom unique et renvoie le
     * chemin absolu sur le disque.
     */
    public String store(MultipartFile file) {
        String unique = UUID.randomUUID() + ".mp3";
        Path target = storageDir.resolve(unique);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec d'enregistrement du fichier : " + unique, e);
        }
        return target.toString();
    }

    public Path resolve(String cheminFichier) {
        return Path.of(cheminFichier);
    }

    public void delete(String cheminFichier) {
        try {
            Files.deleteIfExists(Path.of(cheminFichier));
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de suppression du fichier : " + cheminFichier, e);
        }
    }
}
