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
import javax.microedition.io.DatagramConnection;

/**
 *
 * @author florent
 */
public class Streams {

    public static StreamConnection serial(int port, int speed) throws IOException {
        return (StreamConnection) Connector.open("comm:com" + port + ";baudrate=" + speed + ";blocking=on;autocts=off;autorts=off");
    }

    public static StreamConnection tcp(String host, int port) throws IOException {
        return (StreamConnection) Connector.open("socket://" + host + ":" + port);
    }

    public static DatagramConnection udp(String host, int port) throws IOException {
        return (DatagramConnection) Connector.open("udp://" + host + ":" + port);
    }
}
