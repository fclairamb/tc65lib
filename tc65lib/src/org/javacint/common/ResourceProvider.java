/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.InputConnection;

/**
 *
 * @author florent
 */
public class ResourceProvider implements InputConnection {

    private final Class type;
    private final String name;

    public ResourceProvider(Class classType, String name) {
        this.type = classType;
        this.name = name;
    }

    public String toString() {
        return type.getName() + ":\"" + name + "\"";
    }

    public InputStream openInputStream() throws IOException {
        return type.getResourceAsStream(name);
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public void close() throws IOException {
    }
}
