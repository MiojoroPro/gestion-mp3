package com.gestionmp3.lister;

import com.gestionmp3.common.config.AppConfig;
import com.gestionmp3.common.config.RabbitConstants;
import com.gestionmp3.common.model.Mp3File;
import com.gestionmp3.common.rabbit.RabbitClient;
import com.gestionmp3.common.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Programme 1 du back office : le Lister.
 *
 * <p>Scanne le dossier configure ({@code MP3_INBOX_DIR}), liste les fichiers
 * {@code .mp3} et publie un message {@link Mp3File} par fichier sur la file
 * {@code mp3.discovered}. C'est un producteur a usage unique : il scanne une
 * fois, publie, journalise puis se termine.
 */
public class ListerApp {

    private static final Logger log = LoggerFactory.getLogger(ListerApp.class);

    public static void main(String[] args) {
        Path inbox = Path.of(AppConfig.mp3InboxDir());
        log.info("=== Demarrage du Lister ===");
        log.info("Dossier scanne : {}", inbox.toAbsolutePath());

        if (!Files.isDirectory(inbox)) {
            log.error("Le dossier '{}' n'existe pas ou n'est pas un repertoire. Arret.", inbox.toAbsolutePath());
            System.exit(1);
        }

        try (RabbitClient rabbit = new RabbitClient()) {
            List<Path> mp3Files = listMp3Files(inbox);
            log.info("{} fichier(s) .mp3 trouve(s).", mp3Files.size());

            int published = 0;
            for (Path file : mp3Files) {
                try {
                    Mp3File message = new Mp3File(
                            file.toAbsolutePath().toString(),
                            file.getFileName().toString(),
                            Files.size(file));
                    rabbit.publish(RabbitConstants.ROUTING_DISCOVERED, Json.toBytes(message));
                    published++;
                    log.info("Publie : {} ({} octets)", message.fileName(), message.sizeBytes());
                } catch (IOException e) {
                    log.error("Echec de publication pour '{}' : {}", file, e.getMessage(), e);
                }
            }

            log.info("=== Lister termine : {}/{} fichier(s) publie(s) ===", published, mp3Files.size());
        } catch (Exception e) {
            log.error("Erreur fatale du Lister : {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static List<Path> listMp3Files(Path inbox) throws IOException {
        try (Stream<Path> stream = Files.list(inbox)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mp3"))
                    .sorted()
                    .toList();
        }
    }
}
