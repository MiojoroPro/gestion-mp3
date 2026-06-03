package com.gestionmp3.envoyeur;

import com.gestionmp3.common.config.AppConfig;
import com.gestionmp3.common.config.RabbitConstants;
import com.gestionmp3.common.model.Mp3File;
import com.gestionmp3.common.model.Mp3Metadata;
import com.gestionmp3.common.rabbit.RabbitClient;
import com.gestionmp3.common.util.Json;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/**
 * Programme 3 du back office : l'Envoyeur.
 *
 * <p>Consomme les messages {@link Mp3Metadata} de la file {@code mp3.metadata},
 * envoie le fichier MP3 et ses metadonnees a l'API web via une requete
 * {@code multipart/form-data}. En cas de succes (HTTP 2xx), publie un message
 * {@link Mp3File} sur la file {@code mp3.sent} pour declencher la suppression.
 */
public class EnvoyeurApp {

    private static final Logger log = LoggerFactory.getLogger(EnvoyeurApp.class);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        String apiUrl = AppConfig.apiUrl();
        log.info("=== Demarrage de l'Envoyeur (en attente de messages) ===");
        log.info("API cible : {}", apiUrl);

        RabbitClient rabbit = new RabbitClient();
        Channel channel = rabbit.channel();
        channel.basicQos(1);

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("=== Arret de l'Envoyeur ===");
            shutdown.countDown();
        }));

        DeliverCallback onMessage = (consumerTag, delivery) -> {
            long tag = delivery.getEnvelope().getDeliveryTag();
            Mp3Metadata metadata = null;
            try {
                metadata = Json.from(delivery.getBody(), Mp3Metadata.class);
                log.info("Envoi de '{}' a l'API...", metadata.fileName());

                int status = send(apiUrl, metadata);
                if (status >= 200 && status < 300) {
                    // Succes : on previent le Suppresseur que ce fichier peut etre efface.
                    Mp3File sent = new Mp3File(metadata.path(), metadata.fileName(), metadata.sizeBytes());
                    rabbit.publish(RabbitConstants.ROUTING_SENT, Json.toBytes(sent));
                    channel.basicAck(tag, false);
                    log.info("'{}' envoye (HTTP {}). Signale pour suppression.", metadata.fileName(), status);
                } else {
                    // Echec cote API : on rejette sans suppression pour ne pas perdre le fichier.
                    log.error("API a repondu HTTP {} pour '{}'. Fichier conserve.", status, metadata.fileName());
                    channel.basicNack(tag, false, false);
                }
            } catch (Exception e) {
                String name = metadata != null ? metadata.fileName() : "?";
                log.error("Echec d'envoi pour '{}' : {}", name, e.getMessage(), e);
                channel.basicNack(tag, false, false);
            }
        };

        channel.basicConsume(RabbitConstants.QUEUE_METADATA, false, onMessage, consumerTag -> {});

        shutdown.await();
        rabbit.close();
    }

    /** Construit et envoie la requete multipart. Renvoie le code HTTP. */
    private static int send(String apiUrl, Mp3Metadata metadata) throws Exception {
        // Une seule instance : le corps et le Content-Type partagent ainsi le meme boundary.
        MultipartBody multipart = new MultipartBody();
        byte[] body = multipart
                .addTextPart("metadata", "application/json", Json.toString(metadata))
                .addFilePart("file", Path.of(metadata.path()))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", multipart.contentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}
