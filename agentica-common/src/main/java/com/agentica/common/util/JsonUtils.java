package com.agentica.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * Utility class for JSON serialization and deserialization.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        return mapper;
    }

    /**
     * Returns the shared ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Converts an object to its JSON string representation.
     */
    public static Optional<String> toJson(Object object) {
        if (object == null) {
            return Optional.empty();
        }

        try {

            return Optional.of(OBJECT_MAPPER.writeValueAsString(object));

        } catch (JsonProcessingException e) {

            log.error("Failed to serialize object to JSON, error: {}", e.getMessage(), e);

            return Optional.empty();
        }
    }

    /**
     * Converts an object to its pretty-printed JSON string representation.
     */
    public static Optional<String> toPrettyJson(Object object) {
        if (object == null) {
            return Optional.empty();
        }

        try {

            return Optional.of(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object));

        } catch (JsonProcessingException e) {

            log.error("Failed to serialize object to pretty JSON, error: {}", e.getMessage(), e);

            return Optional.empty();
        }
    }

    /**
     * Parses a JSON string into an object of the specified class.
     */
    public static <T> Optional<T> fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        try {

            return Optional.of(OBJECT_MAPPER.readValue(json, clazz));

        } catch (JsonProcessingException e) {

            log.error("Failed to deserialize JSON to {}, error: {}", clazz.getSimpleName(), e.getMessage(), e);

            return Optional.empty();
        }
    }

    /**
     * Parses a JSON string into an object using a TypeReference.
     */
    public static <T> Optional<T> fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        try {

            return Optional.of(OBJECT_MAPPER.readValue(json, typeReference));

        } catch (JsonProcessingException e) {

            log.error("Failed to deserialize JSON, error: {}", e.getMessage(), e);

            return Optional.empty();
        }
    }

    /**
     * Converts an object to a Map.
     */
    public static Map<String, Object> toMap(Object object) {
        return OBJECT_MAPPER.convertValue(object, new TypeReference<>() {});
    }

    /**
     * Converts a Map to an object of the specified class.
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

}
