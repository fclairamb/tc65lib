package org.javacint.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.InputConnection;

/**
 * Resource provider.
 *
 * This is an InputConnection from a class and a path.
 */
public class ResourceProvider implements InputConnection {

    /**
     * Class
     */
    private final Class type;
    /**
     * Resource path
     */
    private final String path;

    public ResourceProvider(Class classType, String path) {
        this.type = classType;
        this.path = path;
    }

    public String toString() {
        return type.getName() + ":\"" + path + "\"";
    }

    public InputStream openInputStream() throws IOException {
        return type.getResourceAsStream(path);
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public void close() throws IOException {
    }
}
