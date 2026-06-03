package com.gestionmp3.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Une musique : ses metadonnees sont en base, le binaire MP3 sur le disque
 * (champ {@link #cheminFichier}).
 */
@Entity
@Table(name = "musique")
public class Musique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String artiste;
    private String album;
    private String genre;
    private String langue;
    private String annee;

    @Column(name = "duree_secondes")
    private int dureeSecondes;

    private String bitrate;

    @Column(name = "numero_piste")
    private String numeroPiste;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "taille_octets")
    private long tailleOctets;

    /** Chemin du fichier MP3 sur le disque serveur. */
    @Column(name = "chemin_fichier", nullable = false)
    private String cheminFichier;

    @Column(name = "date_ajout", nullable = false)
    private Instant dateAjout = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getArtiste() {
        return artiste;
    }

    public void setArtiste(String artiste) {
        this.artiste = artiste;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public int getDureeSecondes() {
        return dureeSecondes;
    }

    public void setDureeSecondes(int dureeSecondes) {
        this.dureeSecondes = dureeSecondes;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getNumeroPiste() {
        return numeroPiste;
    }

    public void setNumeroPiste(String numeroPiste) {
        this.numeroPiste = numeroPiste;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public long getTailleOctets() {
        return tailleOctets;
    }

    public void setTailleOctets(long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public Instant getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Instant dateAjout) {
        this.dateAjout = dateAjout;
    }
}
