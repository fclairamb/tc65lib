package org.javacint.io;

import java.io.IOException;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.StreamConnection;
import org.javacint.logging.Logger;

public class ConnectionsListener extends Thread {

    private final ServerSocketConnection socket;
    private final ConnectionHandler handler;

    public ConnectionsListener(ServerSocketConnection socket, ConnectionHandler handler) {
        super("ConnectionsListener");
        this.socket = socket;
        this.handler = handler;
    }

    public void run() {
        while (true) {
            try {
                StreamConnection sc = socket.acceptAndOpen();
                handler.handleConnection(sc);
            } catch (IOException ex) {
                Logger.log("ConnectionsListener.run", ex, true);
                break;
            }
        }
    }
}
