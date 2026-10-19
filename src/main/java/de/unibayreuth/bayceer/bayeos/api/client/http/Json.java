package de.unibayreuth.bayceer.bayeos.api.client.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Shared Jackson configuration for the client.
 *
 * <p>Timestamps are written as RFC3339 strings but read leniently: the BayEOS
 * API accepts and returns timestamps either as RFC3339 strings or as epoch
 * microseconds, so the {@link Instant} deserializer accepts both encodings.</p>
 */
public final class Json {

    private static final ObjectMapper MAPPER = create();

    private Json() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule timestamps = new SimpleModule();
        timestamps.addDeserializer(Instant.class, new LenientInstantDeserializer());
        mapper.registerModule(timestamps);
        return mapper;
    }

    /**
     * Reads an {@link Instant} from either an RFC3339 string or an epoch
     * microsecond integer.
     */
    static final class LenientInstantDeserializer extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() != null && p.currentToken().isNumeric()) {
                return Instant.EPOCH.plus(p.getLongValue(), ChronoUnit.MICROS);
            }
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException e) {
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            }
        }
    }
}
