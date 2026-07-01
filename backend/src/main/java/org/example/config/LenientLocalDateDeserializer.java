package org.example.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private final DateTimeFormatter primary;

    public LenientLocalDateDeserializer(DateTimeFormatter primary) {
        this.primary = primary;
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = parser.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();

        // 1) Formato oficial dd/MM/yyyy.
        try {
            return LocalDate.parse(text, primary);
        } catch (Exception ignored) { }

        // 2) ISO com offset/hora (antigo java.util.Date), ex.: 2026-06-09T23:43:50.761+00:00.
        try {
            return OffsetDateTime.parse(text).toLocalDate();
        } catch (Exception ignored) { }

        // 3) ISO simples yyyy-MM-dd.
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) { }

        throw new IOException("Formato de data nao reconhecido: " + text);
    }
}
