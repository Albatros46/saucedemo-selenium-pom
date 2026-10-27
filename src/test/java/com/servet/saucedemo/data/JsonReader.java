package com.servet.saucedemo.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** JSON test verisini tipli Java nesnelerine çevirir. */
public final class JsonReader {

    // Bilinmeyen bir alan (ör. yazım hatası "prise") sessizce yok sayılmasın, test hemen patlasın.
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonReader() {
    }

    public static <T> T read(String classpathResource, Class<T> type) {
        try (InputStream in = Objects.requireNonNull(JsonReader.class.getResourceAsStream(classpathResource),
                "JSON dosyası bulunamadı: " + classpathResource)) {
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
