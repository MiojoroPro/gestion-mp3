package com.gestionmp3.api.repository;

import com.gestionmp3.api.model.Musique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Acces aux musiques. {@link JpaSpecificationExecutor} permet de filtrer
 * dynamiquement (artiste, langue, genre...) pour la generation de playlists.
 */
public interface MusiqueRepository extends JpaRepository<Musique, Long>, JpaSpecificationExecutor<Musique> {
}
