package com.gestionmp3.extracteur;

import com.gestionmp3.common.config.RabbitConstants;
import com.gestionmp3.common.model.Mp3File;
import com.gestionmp3.common.model.Mp3Metadata;
import com.gestionmp3.common.rabbit.RabbitClient;
import com.gestionmp3.common.util.Json;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Programme 2 du back office : l'Extracteur.
 *
 * <p>Consomme les messages {@link Mp3File} de la file {@code mp3.discovered},
 * extrait les metadonnees du fichier avec jaudiotagger, puis publie un message
 * {@link Mp3Metadata} sur la file {@code mp3.metadata}. Consumer long : reste a
 * l'ecoute jusqu'a interruption (Ctrl+C).
 */
public class ExtracteurApp {

    private static final Logger log = LoggerFactory.getLogger(ExtracteurApp.class);

    public static void main(String[] args) throws Exception {
        log.info("=== Demarrage de l'Extracteur (en attente de messages) ===");

        RabbitClient rabbit = new RabbitClient();
        Channel channel = rabbit.channel();
        channel.basicQos(1); // un message a la fois : repartition equitable

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("=== Arret de l'Extracteur ===");
            shutdown.countDown();
        }));

        DeliverCallback onMessage = (consumerTag, delivery) -> {
            long tag = delivery.getEnvelope().getDeliveryTag();
            Mp3File discovered = null;
            try {
                discovered = Json.from(delivery.getBody(), Mp3File.class);
                log.info("Extraction des metadonnees de : {}", discovered.fileName());

                Mp3Metadata metadata = extract(discovered);
                rabbit.publish(RabbitConstants.ROUTING_METADATA, Json.toBytes(metadata));
                channel.basicAck(tag, false);

                log.info("Metadonnees publiees : titre='{}', artiste='{}', duree={}s",
                        metadata.title(), metadata.artist(), metadata.durationSeconds());
            } catch (Exception e) {
                String name = discovered != null ? discovered.fileName() : "?";
                log.error("Echec d'extraction pour '{}' : {}", name, e.getMessage(), e);
                // On rejette sans remise en file pour eviter une boucle sur un fichier corrompu.
                channel.basicNack(tag, false, false);
            }
        };

        channel.basicConsume(RabbitConstants.QUEUE_DISCOVERED, false, onMessage, consumerTag -> {});

        shutdown.await();
        rabbit.close();
    }

    /** Lit les tags ID3 du fichier et construit le message de metadonnees. */
    private static Mp3Metadata extract(Mp3File discovered) throws Exception {
        File file = new File(discovered.path());
        if (!file.isFile()) {
            throw new IllegalStateException("Fichier introuvable : " + discovered.path());
        }

        AudioFile audioFile = AudioFileIO.read(file);
        AudioHeader header = audioFile.getAudioHeader();
        Tag tag = audioFile.getTag();

        return new Mp3Metadata(
                discovered.path(),
                discovered.fileName(),
                discovered.sizeBytes(),
                field(tag, FieldKey.TITLE),
                field(tag, FieldKey.ARTIST),
                field(tag, FieldKey.ALBUM),
                field(tag, FieldKey.GENRE),
                field(tag, FieldKey.LANGUAGE),
                field(tag, FieldKey.YEAR),
                header != null ? header.getTrackLength() : 0,
                header != null ? header.getBitRate() : "",
                field(tag, FieldKey.TRACK));
    }

    /** Lecture defensive d'un champ : renvoie "" si absent ou non supporte. */
    private static String field(Tag tag, FieldKey key) {
        if (tag == null) {
            return "";
        }
        try {
            String value = tag.getFirst(key);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
