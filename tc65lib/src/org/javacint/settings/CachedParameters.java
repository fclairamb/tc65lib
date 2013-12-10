package org.javacint.settings;

import java.io.IOException;
import org.javacint.common.PropertiesFile;

/**
 * Cached parameters.
 * These are the parameters that are worth saving but we can still get rid of
 * without disturbing the program.
 */
public class CachedParameters {

    private static final String FILENAME = "parameters.txt";

    public void set(String name, String value) throws IOException {
        PropertiesFile props = new PropertiesFile(FILENAME);
        props.set(name, value);
        props.save();
    }

    public String get(String name, String defaultValue) throws IOException {
        PropertiesFile props = new PropertiesFile(FILENAME);
        return props.get(name, defaultValue);
    }

    public String get(String name) throws IOException {
        return get(name, null);
    }
}
