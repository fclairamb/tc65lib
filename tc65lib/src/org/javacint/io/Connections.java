package org.javacint.io;

import java.io.IOException;
import javax.microedition.io.*;

/**
 * Connector wrapper.
 */
public class Connections {

    /**
     * Serial connection opener.
     *
     * @param port Serial port number (0 for ASC0, 1 for ASC1)
     * @param speed Speed Speed
     * @return CommConnection instance
     * @throws IOException
     */
    public static CommConnection serial(int port, int speed) throws IOException {
        return (CommConnection) Connector.open("comm:com" + port + ";baudrate=" + speed + ";blocking=on;autocts=off;autorts=off");
    }

    /**
     * Socket connection opener.
     *
     * @param host Target host + port
     * @return SocketConnection
     * @throws IOException
     */
    public static SocketConnection tcp(String host) throws IOException {
        return (SocketConnection) Connector.open("socket://" + host);
    }

    /**
     * Socket connection opener.
     *
     * @param host Target host
     * @param port Target port
     * @return SocketConnection instance
     * @throws IOException
     */
    public static SocketConnection tcp(String host, int port) throws IOException {
        return tcp(host + ":" + port);
    }

    /**
     * Server socket connection opener.
     *
     * @param port Port to listen to
     * @return ServerSocketConnection instance
     * @throws IOException
     */
    public static ServerSocketConnection tcpListen(int port) throws IOException {
        return (ServerSocketConnection) Connector.open("socket://:" + port);
    }

    /**
     * UDP datagram connction opener
     *
     * @param host Target host
     * @param port Target port
     * @return DatagramConnection instance
     * @throws IOException
     */
    public static DatagramConnection udp(String host, int port) throws IOException {
        return (DatagramConnection) Connector.open("datagram://" + host + ":" + port);
    }
}
