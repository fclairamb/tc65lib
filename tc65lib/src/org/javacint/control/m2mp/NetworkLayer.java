package org.javacint.control.m2mp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.Bytes;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.time.DateManagement;

/**
 * Network layer
 *
 * @author Florent Clairambault / www.webingenia.com
 */
class NetworkLayer {

    private String clientId;
    private SocketConnection socket;
    private InputStream tcpIs;
    private OutputStream tcpOs;
    private long lastDataSendTime = DateManagement.time(), lastDataRecvTime = DateManagement.time();

    void setIdent(String ident) {
        clientId = ident;
    }

    public void setProtoLayer(FrameType proto) {
    }

    /**
     * Takes care of sending data
     */
    private class NetworkSend implements Runnable {

        final Vector dataOutQueue = new Vector();

        public void run() {
            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                Logger.log("Started!");
            }
            try {
                while (true) {
                    if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                        Logger.log("...");
                    }
                    try {
                        if (dataOutQueue.size() > 0) {
                            byte[] data = (byte[]) dataOutQueue.elementAt(0);
                            if (send(data)) {
                                dataOutQueue.removeElement(data);
                            } else {
                                reconnect();
                            }
                        } else {
                            synchronized (dataOutQueue) {
                                dataOutQueue.wait();
                            }
                        }
                    } catch (Exception ex) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this + ".run", ex);
                        }
                        break;
                    }
                }
            } finally {
                if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                    Logger.log("End!");
                }
            }
        }

        /**
         * Sends some data
         *
         * @param data Data to send
         * @return if the data could be sent
         */
        private boolean send(byte[] data) {
            try {
                if (!isConnected()) {
                    return false;
                }

                tcpOs.write(data);
                tcpOs.flush();
                return true;
            } catch (Exception ex) {
                if (Logger.BUILD_WARNING) {
                    Logger.log(this + ".send", ex);
                }
                return false;
            }
        }

        public void queueSend(byte[] data) {
            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                Logger.log(this + ".queueSend( byte[" + data.length + "] );");
            }

            synchronized (dataOutQueue) {
                dataOutQueue.addElement(data);
                dataOutQueue.notify();
            }
        }

        private void stop(boolean b) {
        }

        public String toString() {
            return "NetworkSend";
        }
    }

    private void reconnect() {

        disconnect();

        connect();
    }

    /**
     * Takes care of receiving data
     */
    private class NetworkReceive implements Runnable {



        private void stop(boolean b) {
        }

        public String toString() {
            return "NetworkReceive";
        }
    }

    /**
     * The constructor
     */
    NetworkLayer() {
        send = new NetworkSend();
        recv = new NetworkReceive();
    }

    void start() {
        new Thread(send, "m2mp-send").start();
        new Thread(recv, "m2mp-recv").start();
    }
    
    long lastConnectCall = 0;
    int nbConnectAttempts = 0;

    /**
     * Connects to a server
     *
     * @return TRUE if the connection worked, FALSE if it didn't
     */
    public synchronized boolean connect() {
        try {
            if (isConnected()) {
                return true;
            }

            if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                Logger.log(this + ".connect();");
            }

            // We don't want this to be called too soon !
            if (Math.abs(DateManagement.time() - lastConnectCall) < 10) {
                if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                    Logger.log(this + ".connect: Too soon !");
                }
                Thread.sleep(1000);
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

            //OnDisconnected();

            // We have to make sure receiving won't be messed up
            // by last connection
            recv.resetFrame();

            try {
                String servers[] = Strings.split(',', Settings.get(M2MPClientImpl.SETTING_M2MP_SERVERS));
                for (int i = 0; i < servers.length; ++i) {
                    String server = servers[i];
                    try {
                        socket = (SocketConnection) Connector.open("socket://" + server);
                        //_socket.setSocketOption(SocketConnection.KEEPALIVE, 1);
                        tcpIs = socket.openInputStream();
                        tcpOs = socket.openOutputStream();

                        if (Logger.BUILD_NOTICE && M2MPClientImpl.m2mpLog_) {
                            Logger.log(this + ".connect: Successfully connected to " + server);
                        }

                        tcpOs.write(identMessage());
                        //CheckIfSendVersionChange();
                        tcpOs.flush();

                        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                            Logger.log(this + ".connect: Identification request sent...");
                        }

                        lastDataRecvTime = DateManagement.time();
                        lastDataSendTime = DateManagement.time();

                        nbConnectAttempts = 0;
                        return true;
                    } catch (Exception ex) {
                        if (Logger.BUILD_WARNING) {
                            Logger.log(this + ".connect: Could not connect to " + server, ex);
                        }
                    }
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

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (socket == null) {
            return;
        }
        try {
            if (tcpIs != null) {
                tcpIs.close();
            }
        } catch (Exception ex) {
            Logger.log(this + ".disconnect()/1;", ex);
        }

        try {
            if (tcpOs != null) {
                tcpOs.close();
            }
        } catch (Exception ex) {
            Logger.log(this + ".disconnect()/2;", ex);
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {
            Logger.log(this + ".disconnect()/3;", ex);
        }

        socket = null;
        tcpIs = null;
        tcpOs = null;

        onDisconnected();
    }

    public boolean isConnected() {
        return socket != null;
    }

    void dispose() {
        recv.stop(false);
        send.stop(false);
        disconnect();
    }
    private final NetworkSend send;
    private final NetworkReceive recv;

    /**
     * Send a frame
     *
     * @param data frame data
     */
    public void sendFrame(byte[] data) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendFrame( " + Bytes.byteArrayToPrettyString(data) + " );");
        } else if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendFrame( byte[" + data.length + "] );");
        }

        lastDataSendTime = DateManagement.time();

        send.queueSend(data);
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

    protected void onDisconnected() {
        if (protocolLayer != null) {
            protocolLayer.disconnected();
        }
    }
    private ProtocolLayer protocolLayer;

    public void setProtoLayer(ProtocolLayer proto) {
        protocolLayer = proto;
    }

    public String toString() {
        return "NetworkLayer";
    }
}
