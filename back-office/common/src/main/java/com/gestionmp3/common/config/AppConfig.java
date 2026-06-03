package com.gestionmp3.common.config;

/**
 * Lecture centralisee de la configuration via variables d'environnement,
 * avec valeurs par defaut adaptees a un poste de developpement local.
 */
public final class AppConfig {

    private AppConfig() {
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    // --- RabbitMQ ---
    public static String rabbitHost() {
        return env("RABBIT_HOST", "localhost");
    }

    public static int rabbitPort() {
        return Integer.parseInt(env("RABBIT_PORT", "5672"));
    }

    public static String rabbitUser() {
        return env("RABBIT_USER", "guest");
    }

    public static String rabbitPassword() {
        return env("RABBIT_PASS", "guest");
    }

    // --- Lister ---
    /** Dossier scanne par le Lister pour y trouver les fichiers .mp3. */
    public static String mp3InboxDir() {
        return env("MP3_INBOX_DIR", "./mp3-inbox");
    }

    // --- Envoyeur ---
    /** Endpoint de l'API web qui recoit les MP3 et leurs metadonnees. */
    public static String apiUrl() {
        return env("API_URL", "http://localhost:8080/api/musiques/import");
    }
}
