package org.javacint.control.m2mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;
import org.javacint.control.m2mp.data.AcknowledgeRequest;
import org.javacint.control.m2mp.data.Disconnected;
import org.javacint.control.m2mp.data.Event;
import org.javacint.control.m2mp.data.IdentificationRequest;
import org.javacint.control.m2mp.data.Message;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.time.DateManagement;

/**
 * Network layer
 *
 * @author Florent Clairambault / www.webingenia.com
 */
class NetworkHandler {
    // Core threads

    private final NetworkReceive recv;
    private final NetworkSend send;
    // Identifier
    private String ident = "__DEFAULT__";
    private SocketConnection socket;
    // M2MP protocol converters
    private M2MPReader reader;
    private M2MPWriter writer;
    private long lastDataSendTime = DateManagement.time();
    private long lastDataRecvTime = DateManagement.time();
    private long lastConnectCall = 0;
    private int nbConnectAttempts = 0;
    private static final int // States:
            STATE_INITIAL = 0,
            STATE_CONNECTED = 1,
            STATE_IDENTIFIED = 2;
    private int state;

    void setIdent(String ident) {
        this.ident = ident;
    }

    String getIdent() {
        return ident;
    }

    void stop() {
    }
    private int keepAlive = 600;

    void setKeepAlive(int aInt) {
        keepAlive = aInt;
    }
    private M2MPEventsListener listener;

    public void setListener(M2MPEventsListener listener) {
        this.listener = listener;
    }

    /**
     * Takes care of sending data.
     *
     * This class is permanently loaded.
     */
    private class NetworkSend implements Runnable {

        final Vector dataOutQueue = new Vector();

        public void run() {
            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                Logger.log(this + ".run: started");
            }
            try {
                while (true) {
                    try {
                        consider();
                        synchronized (dataOutQueue) {
                            if (writer != null && dataOutQueue.size() > 0) {
                                Message msg = (Message) dataOutQueue.elementAt(0);
                                if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                                    Logger.log(this + ".run: Sending... / size = " + dataOutQueue.size());
                                }
                                writer.write(msg);
                                dataOutQueue.removeElement(msg);
                            } else {
                                long w = keepAlive * 1000 / 3;
                                if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                                    Logger.log(this + ".run: Sleeping " + w + " ms.");
                                }
                                dataOutQueue.wait(w);
                            }
                        }
                    } catch (Exception ex) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this + ".run", ex);
                        }
                        writer = null;
                    }
                }
            } finally {
                if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                    Logger.log(this + ".run: stopped");
                }
            }
        }
        private byte requestNb;

        private void consider() {
            long time = DateManagement.time();
            if (time - lastDataRecvTime > keepAlive && time - lastDataSendTime > (keepAlive / 2)) {
                queue(new AcknowledgeRequest(requestNb++));
            }
            if (writer == null) {
                connect();
            }
        }

        public void queue(Event evt) {
            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                Logger.log(this + ".queue( " + evt + " ); / size = " + dataOutQueue.size());
            }
            lastDataSendTime = DateManagement.time();
            synchronized (dataOutQueue) {
                dataOutQueue.addElement(evt);
                dataOutQueue.notify();
            }
        }

        /**
         * Connects to a server
         *
         * @return TRUE if the connection worked, FALSE if it didn't
         */
        public synchronized boolean connect() {
            try {
                disconnect();
                state = STATE_INITIAL;
                long time = DateManagement.time();
                if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                    Logger.log(this + ".connect(); / nbAttempts = " + nbConnectAttempts);
                }

                // We don't want this to be called too soon !
                if (Math.abs(time - lastConnectCall) < 10) {
                    if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                        Logger.log(this + ".connect: Too soon !");
                    }
                    Thread.sleep(5000);
                    return false;
                }


                if (++nbConnectAttempts % 3 == 0) {
                    if (Logger.BUILD_NOTICE && M2MPClientImpl.m2mpLog_) {
                        Logger.log(this + ".connect: We need to quit and reconnect to the GSM Network...");
                    }
                    try {
                        ATExecution.setAirplaneMode(true);
                        Thread.sleep(5000);
                        ATExecution.setAirplaneMode(false);
                        Thread.sleep(15000);
                    } catch (Exception ex) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this + ".connect:296", ex);
                        }
                    }
                    if (Logger.BUILD_NOTICE && M2MPClientImpl.m2mpLog_) {
                        Logger.log(this + ".connect: OK");
                    }
                }

                try {
                    String servers[] = Strings.split(',', Settings.get(M2MPClientImpl.SETTING_M2MP_SERVERS));
                    for (int i = 0; i < servers.length; ++i) {
                        String server = servers[i];
                        try {
                            socket = (SocketConnection) Connector.open("socket://" + server);
                            InputStream is = socket.openInputStream();
                            OutputStream os = socket.openOutputStream();

                            reader = new M2MPReader(is);
                            writer = new M2MPWriter(os);

                            state = STATE_CONNECTED;

                            if (Logger.BUILD_NOTICE && M2MPClientImpl.m2mpLog_) {
                                Logger.log(this + ".connect: Successfully connected to " + server);
                            }

                            writer.write(new IdentificationRequest(ident));

                            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                                Logger.log(this + ".connect: Identification request sent...");
                            }


                            startRecv();

                            lastDataRecvTime = DateManagement.time();
                            lastDataSendTime = DateManagement.time();

                            nbConnectAttempts = 0;
                            return true;
                        } catch (Exception ex) {
                            if (Logger.BUILD_WARNING) {
                                Logger.log(this + ".connect: Error connecting to " + server, ex);
                            }
                        }
                    }

                    if (nbConnectAttempts > 50) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this + ".connect: Too many failed attempts, restarting !");
                        }
                        ATExecution.restart();
                    }

                    // It means we don't have any server
                    return false;
                } catch (Exception ex) {
                    if (Logger.BUILD_WARNING) {
                        Logger.log(this + ".connect:38", ex);
                    }
                    return false;
                } finally {
                    try {
                        Thread.sleep(10000);
                    } catch (Exception ex) {
                    }
                    lastConnectCall = DateManagement.time();
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".connect:39", ex);
                }
                return false;
            }
        }

        public String toString() {
            return "NetworkSend";
        }
    }

    /**
     * Takes care of receiving data
     */
    private class NetworkReceive implements Runnable {

        public String toString() {
            return "NetworkReceive";
        }

        public void run() {
            while (true) {
                try {
                    Message msg = reader.read();
                    received(msg);
                } catch (Exception ex) {
                    if (ex instanceof IOException) {
                        received(new Disconnected());
                    }
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".run", ex, true);
                    }
                    break;
                }
            }
        }
    }

    private void received(Event event) {
        lastDataRecvTime = DateManagement.time();
        listener.m2mpEvent(event);
    }

    /**
     * The constructor
     */
    NetworkHandler() {
        send = new NetworkSend();
        recv = new NetworkReceive();
    }

    NetworkHandler(M2MPEventsListener listener) {
        this();
        this.listener = listener;
    }

    void start() {
        startSend();
    }

    private void startSend() {
        new Thread(send, "m2mp-send").start();
    }

    private void startRecv() {
        new Thread(recv, "m2mp-recv").start();
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        try {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    Logger.log(this + ".disconnect:1", ex);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    Logger.log(this + ".disconnect:2", ex);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    Logger.log(this + ".disconnect:3", ex);
                }
            }
        } catch (Exception ex) {
            Logger.log(this + ".disconnect:4", ex);
        }
    }

    /**
     * Send an event.
     */
    public void send(Event evt) {
        lastDataSendTime = DateManagement.time();
        send.queue(evt);
    }

    public long getLastRecvTime() {
        return lastDataRecvTime;
    }

    public void updateLastRecvTime() {
        lastDataRecvTime = DateManagement.time();
    }

    public long getLastSendTime() {
        return lastDataSendTime;
    }

    public long getLastConnectCall() {
        return lastConnectCall;
    }

    public String toString() {
        return "NetworkLayer";
    }
}
