package com.gestionmp3.common.config;

/**
 * Topologie RabbitMQ partagee par tous les programmes du back office.
 *
 * <pre>
 *  [Lister] --(discovered)--> [Extracteur] --(metadata)--> [Envoyeur] --(sent)--> [Suppresseur]
 * </pre>
 *
 * On utilise un exchange direct unique et trois files durables, une par etape.
 */
public final class RabbitConstants {

    /** Exchange direct partage par toutes les etapes. */
    public static final String EXCHANGE = "mp3.exchange";

    /** File des MP3 decouverts par le Lister, consommee par l'Extracteur. */
    public static final String QUEUE_DISCOVERED = "mp3.discovered";
    public static final String ROUTING_DISCOVERED = "discovered";

    /** File des metadonnees extraites, consommee par l'Envoyeur. */
    public static final String QUEUE_METADATA = "mp3.metadata";
    public static final String ROUTING_METADATA = "metadata";

    /** File des fichiers envoyes avec succes, consommee par le Suppresseur. */
    public static final String QUEUE_SENT = "mp3.sent";
    public static final String ROUTING_SENT = "sent";

    private RabbitConstants() {
    }
}
