package com.gestionmp3.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Serialisation / deserialisation JSON partagee (Jackson). */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Json() {
    }

    public static byte[] toBytes(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de serialisation JSON", e);
        }
    }

    public static String toString(Object value) {
        return new String(toBytes(value), StandardCharsets.UTF_8);
    }

    public static <T> T from(byte[] body, Class<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de deserialisation JSON", e);
        }
    }
}
