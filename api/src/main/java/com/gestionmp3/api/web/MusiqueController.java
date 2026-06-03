package com.gestionmp3.api.web;

import com.gestionmp3.api.dto.ImportMetadata;
import com.gestionmp3.api.dto.MusiqueDto;
import com.gestionmp3.api.dto.MusiqueUpdate;
import com.gestionmp3.api.model.Musique;
import com.gestionmp3.api.service.MusiqueService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

/** Endpoints CRUD + import + streaming des musiques. */
@RestController
@RequestMapping("/api/musiques")
public class MusiqueController {

    private final MusiqueService service;

    public MusiqueController(MusiqueService service) {
        this.service = service;
    }

    /** Import depuis le back office (Envoyeur) : multipart metadata + fichier. */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MusiqueDto> importer(
            @RequestPart("metadata") ImportMetadata metadata,
            @RequestPart("file") MultipartFile file) {
        Musique saved = service.importMusique(metadata, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(MusiqueDto.from(saved));
    }

    @GetMapping
    public List<MusiqueDto> lister() {
        return service.findAll().stream().map(MusiqueDto::from).toList();
    }

    @GetMapping("/{id}")
    public MusiqueDto obtenir(@PathVariable Long id) {
        return MusiqueDto.from(service.findById(id));
    }

    @PutMapping("/{id}")
    public MusiqueDto modifier(@PathVariable Long id, @RequestBody MusiqueUpdate update) {
        return MusiqueDto.from(service.update(id, update));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Flux audio pour la lecture (play/pause) dans le navigateur. */
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamer(@PathVariable Long id) {
        Musique musique = service.findById(id);
        Path path = Path.of(musique.getCheminFichier());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new NotFoundException("Fichier audio absent pour la musique " + id);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + musique.getNomFichier() + "\"")
                .body(resource);
    }
}
