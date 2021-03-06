package configuration.objectmapping;

import configuration.ConfigOptions;

public interface ObjectMapper<T> {

    boolean canMap(Class<?> type);

    /**
     * @param options
     * @param value
     * @return Must be {@link Number} and its sub-classes, {@link Boolean}, {@link String}, {@link java.util.Map}, {@link java.util.List}
     */
    Object serialize(ConfigOptions options, T value) throws Exception;

    /**
     * @param options
     * @param raw Must be {@link Number} and its sub-classes, {@link Boolean}, {@link String}, {@link java.util.Map}, {@link java.util.List}
     * @return
     */
    T deserialize(ConfigOptions options, Object raw) throws Exception;
}
