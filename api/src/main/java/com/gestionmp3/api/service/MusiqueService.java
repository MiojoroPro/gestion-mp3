package com.gestionmp3.api.service;

import com.gestionmp3.api.dto.ImportMetadata;
import com.gestionmp3.api.dto.MusiqueUpdate;
import com.gestionmp3.api.model.Musique;
import com.gestionmp3.api.repository.MusiqueRepository;
import com.gestionmp3.api.web.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Logique metier autour des musiques : import depuis le back office et CRUD web. */
@Service
public class MusiqueService {

    private final MusiqueRepository repository;
    private final StorageService storage;

    public MusiqueService(MusiqueRepository repository, StorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    /** Import depuis l'Envoyeur : stocke le binaire puis persiste les metadonnees. */
    @Transactional
    public Musique importMusique(ImportMetadata meta, MultipartFile file) {
        String cheminFichier = storage.store(file);

        Musique musique = new Musique();
        musique.setTitre(orFileName(meta.title(), meta.fileName()));
        musique.setArtiste(meta.artist());
        musique.setAlbum(meta.album());
        musique.setGenre(meta.genre());
        musique.setLangue(meta.language());
        musique.setAnnee(meta.year());
        musique.setDureeSecondes(meta.durationSeconds());
        musique.setBitrate(meta.bitrate());
        musique.setNumeroPiste(meta.trackNumber());
        musique.setNomFichier(meta.fileName());
        musique.setTailleOctets(meta.sizeBytes() > 0 ? meta.sizeBytes() : file.getSize());
        musique.setCheminFichier(cheminFichier);

        return repository.save(musique);
    }

    @Transactional(readOnly = true)
    public List<Musique> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Musique findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Musique introuvable : " + id));
    }

    @Transactional
    public Musique update(Long id, MusiqueUpdate update) {
        Musique musique = findById(id);
        if (update.titre() != null) musique.setTitre(update.titre());
        if (update.artiste() != null) musique.setArtiste(update.artiste());
        if (update.album() != null) musique.setAlbum(update.album());
        if (update.genre() != null) musique.setGenre(update.genre());
        if (update.langue() != null) musique.setLangue(update.langue());
        if (update.annee() != null) musique.setAnnee(update.annee());
        return repository.save(musique);
    }

    @Transactional
    public void delete(Long id) {
        Musique musique = findById(id);
        storage.delete(musique.getCheminFichier());
        repository.delete(musique);
    }

    private static String orFileName(String titre, String fileName) {
        if (titre != null && !titre.isBlank()) {
            return titre;
        }
        // Pas de titre dans les tags : on retombe sur le nom de fichier sans extension.
        return fileName != null ? fileName.replaceFirst("(?i)\\.mp3$", "") : "Sans titre";
    }
}
