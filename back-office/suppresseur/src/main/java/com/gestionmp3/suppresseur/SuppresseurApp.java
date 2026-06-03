package com.gestionmp3.suppresseur;

import com.gestionmp3.common.config.RabbitConstants;
import com.gestionmp3.common.model.Mp3File;
import com.gestionmp3.common.rabbit.RabbitClient;
import com.gestionmp3.common.util.Json;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Programme 4 du back office : le Suppresseur.
 *
 * <p>Consomme les messages {@link Mp3File} de la file {@code mp3.sent} (fichiers
 * deja envoyes avec succes a l'API) et supprime le fichier .mp3 correspondant du
 * disque. Consumer long : reste a l'ecoute jusqu'a interruption (Ctrl+C).
 */
public class SuppresseurApp {

    private static final Logger log = LoggerFactory.getLogger(SuppresseurApp.class);

    public static void main(String[] args) throws Exception {
        log.info("=== Demarrage du Suppresseur (en attente de messages) ===");

        RabbitClient rabbit = new RabbitClient();
        Channel channel = rabbit.channel();
        channel.basicQos(1);

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("=== Arret du Suppresseur ===");
            shutdown.countDown();
        }));

        DeliverCallback onMessage = (consumerTag, delivery) -> {
            long tag = delivery.getEnvelope().getDeliveryTag();
            Mp3File sent = null;
            try {
                sent = Json.from(delivery.getBody(), Mp3File.class);
                Path file = Path.of(sent.path());

                boolean deleted = Files.deleteIfExists(file);
                channel.basicAck(tag, false);

                if (deleted) {
                    log.info("Fichier supprime : {}", sent.path());
                } else {
                    log.warn("Fichier deja absent (rien a supprimer) : {}", sent.path());
                }
            } catch (Exception e) {
                String name = sent != null ? sent.fileName() : "?";
                log.error("Echec de suppression pour '{}' : {}", name, e.getMessage(), e);
                channel.basicNack(tag, false, false);
            }
        };

        channel.basicConsume(RabbitConstants.QUEUE_SENT, false, onMessage, consumerTag -> {});

        shutdown.await();
        rabbit.close();
    }
}
