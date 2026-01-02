package io.github.jlmc.poc.commons.settings.json;


import io.github.jlmc.poc.commons.settings.exceptions.DeserializationSettingsClientException;

/**
 * Provides a way to deserialize JSON strings into objects.
 */
public interface JsonDeserializer {

    /**
     * Deserializes the given JSON source into an object of the specified type.
     *
     * @param source The JSON string to deserialize.
     * @param type   The target class type for data deserialization.
     * @param <T>    The type of the resulting object.
     * @return An instance of T deserialized from the JSON string.
     * @throws DeserializationSettingsClientException If the JSON string cannot be deserialized.
     */
    <T> T deserialize(String source, Class<T> type) throws DeserializationSettingsClientException;

    /**
     * Deserializes the given source object (e.g., a JSON node or map) into an object of the specified type.
     *
     * @param source The source object to deserialize.
     * @param type   The target class type for data deserialization.
     * @param <T>    The type of the resulting object.
     * @return An instance of T deserialized from the source object.
     * @throws DeserializationSettingsClientException If the source cannot be deserialized.
     */
    <T> T readValueAs(Object source, Class<T> type) throws DeserializationSettingsClientException;
}
