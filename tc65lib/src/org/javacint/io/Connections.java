package org.javacint.io;

import java.io.IOException;
import javax.microedition.io.*;

public class Connections {

    public static StreamConnection serial(int port, int speed) throws IOException {
        return (StreamConnection) Connector.open("comm:com" + port + ";baudrate=" + speed + ";blocking=on;autocts=off;autorts=off");
    }

    public static StreamConnection tcp(String host) throws IOException {
        return (StreamConnection) Connector.open("socket://" + host);
    }

    public static StreamConnection tcp(String host, int port) throws IOException {
        return tcp(host + ":" + port);
    }

    public static ServerSocketConnection tcpListen(int port) throws IOException {
        return (ServerSocketConnection) Connector.open("socket://:" + port);
    }

    public static DatagramConnection udp(String host, int port) throws IOException {
        return (DatagramConnection) Connector.open("datagram://" + host + ":" + port);
    }
}
