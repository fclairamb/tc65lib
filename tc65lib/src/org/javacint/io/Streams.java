/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 *
 * @author florent
 */
public class Streams {

    public static StreamConnection serial(int port, int speed) throws IOException {
        return Streams.open(new SerialProfile(port, speed));
    }

    public static StreamConnection open(ConnectionProfile conn) throws IOException {
        return (StreamConnection) Connector.open(conn.getProfile());
    }
}
