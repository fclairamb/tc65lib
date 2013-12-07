package org.javacint.control.m2mp;

import com.siemens.icm.io.ATCommand;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;
import org.javacint.common.Bytes;
import org.javacint.logging.Logger;
import org.javacint.time.DateManagement;

/**
 * The protocol layer of the network implementation.
 *
 * We call it ClientSend to simplify the use of the code
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public final class ProtocolLayer implements IProtocolLayer {

    ATCommand _atc;

    /**
     * Constructor
     *
     * @param atc ATCommand instance
     */
    ProtocolLayer(ATCommand atc) {
        _atc = atc;
        disconnected();
    }

    /**
     * Starts the network layer
     */
    void start() {
        try {
            if (net == null) {
                net = new NetworkLayer(_atc);
                net.setProtoLayer(this);
                net.setIdent(clientId);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_DEBUG) {
                Logger.log("ClientSend.start", ex);
            }
        }
    }

    /**
     * Stop the network layer (no reason to do this)
     */
    void stop() {
        net.dispose();
        net = null;
    }
    private String clientId;

    void setIdent(String ident) {
        clientId = ident;
        if (net != null) {
            net.setIdent(ident);
        }
    }

    String getIdent() {
        return clientId;
    }

    public void sendAckResponse(byte b) {
        net.sendFrame(new byte[]{ProtocolLayer.NET_S_ACK_RESPONSE, b});
    }
    byte nbUnrepliedAcks = 0;

    public void sendAckRequest(byte b) {
        net.sendFrame(new byte[]{ProtocolLayer.NET_S_ACK_REQUEST, b});
        nbUnrepliedAcks += 1;
    }
    byte ackRequestIncrement = 0;

    /**
     * Send an ack request
     *
     * The ack request number is automatically incremented
     */
    public void sendAckRequest() {
        sendAckRequest(++ackRequestIncrement);
    }
    int connectionTimeout = 1200;

    private void disconnect() {
        nbUnrepliedAcks = 0;
        net.disconnect();
    }

    private class ConnectionChecker extends TimerTask {

        public void run() {
            checkConnection();
        }
    }
    private ConnectionChecker connChecker;

    /**
     * Connection checker.
     *
     * This method has to be called frequently (each 5 to 15 seconds) by a
     * background thread to check that the connection is still active.
     */
    public void checkConnection() {
        try {
            long time = DateManagement.time();

            if (!net.isConnected() || Math.abs(time - getLastSendTime()) < 5) {
                return;
            }

            long diff = Math.abs(time - getLastRecvTime());

//			if (Logger.BUILD_DEBUG && app.m2mpLog_) {
//				Logger.log(this + ".checkConnection() / diff = " + diff, true);
//			}


            // If this is a timeout, we disconnect
            if (diff > connectionTimeout + 30 && nbUnrepliedAcks > 1) {
                if (Logger.BUILD_NOTICE) {
                    Logger.log("No answer from server for " + Math.abs(time - getLastRecvTime()) + " seconds. Connection timeout is set to " + connectionTimeout + " seconds. Disconnecting !");
                }

                disconnect();
            } // And if this is net yet a timeout but we haven't received anything for some time, we send a ping request
            else if (diff > connectionTimeout) {
                if (Logger.BUILD_VERBOSE) {
                    Logger.log("Sending ack to avoid timeout...");
                }
                sendAckRequest();
            }
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".checkConnection", ex, true);
            }
        }
    }

    private void treatFrameAckRequest(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameAckRequest( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        app.onReceivedAckRequest(frame[1]);
        nbUnrepliedAcks = 0;
    }

    private void treatFrameAckResponse(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameAckResponse( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        app.onReceivedAckResponse(frame[1]);
        nbUnrepliedAcks = 0;
    }

    private void treatFrameChannelDef(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameChannelDef( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        /*
         * Bytes are : =========== 0 : Type 1 : Size 2 : Channel id 3-X :
         * Channel name
         */
        byte channelId = frame[2];

        String channelName = new String(frame, 3, frame.length - 3);

        cmRecv.setName(channelId, channelName);
    }

    private void treatFrameData(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameData( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1 : Size 2 : Channel Id 3-X : Data
         */
        byte channelId = frame[2];
        String channelName = cmRecv.getName(channelId);

        byte[] data = new byte[(frame.length - 3)];
        System.arraycopy(frame, 3, data, 0, data.length);

        app.onReceivedData(channelName, data);
    }

    private void treatFrameDataLarge(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameDataLarge( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1-2 : Size 3 : Channel Id 4-X : Data
         */

        byte channelId = frame[3];
        String channelName = cmRecv.getName(channelId);

        byte[] data = new byte[(frame.length - 4)];
        System.arraycopy(frame, 4, data, 0, data.length);

        app.onReceivedData(channelName, data);
    }

    private void treatFrameDataArray(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameDataArray( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1 : Size 2 : Channel Id 3-X : Data
         */

        // first byte for the size
        int offset = 2;

        byte channelId = frame[offset++];
        String channelName = cmRecv.getName(channelId);

        Vector content = new Vector();

        while (offset < frame.length) {
            int size = frame[offset++];
            byte[] subData = new byte[size];
            System.arraycopy(frame, offset, subData, 0, size);
            offset += size;
            content.addElement(subData);
        }

        byte[][] data = new byte[content.size()][];
        content.copyInto(data);

        app.onReceivedData(channelName, data);
    }

    private void treatFrameDataArrayLarge(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameDataArrayLarge( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1-2 : Size 3 : Channel Id 4-X : Data
         */
        int offset = 3;

        byte channelId = frame[offset++];
        String channelName = cmRecv.getName(channelId);

        Vector content = new Vector();


        while (offset < frame.length) {
            int size = Bytes.bytesToShort(frame, offset);
            offset += 2;

            byte[] subData = new byte[size];
            System.arraycopy(frame, offset, subData, 0, size);
            offset += size;

            content.addElement(subData);
        }

        byte[][] data = new byte[content.size()][];
        content.copyInto(data);

        app.onReceivedData(channelName, data);
    }

    private void treatFrameIdentificationResult(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.TreatFrameIdentificationResult( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        boolean identified = frame[1] == 0x01;
        app.onReceivedIdentificationResult(identified);
    }

    void setKeepAlive(int keepAlive) {
        if (keepAlive > 7200) {
            keepAlive = 7200;
        } else if (keepAlive < 15) {
            keepAlive = 15;
        }

        connectionTimeout = keepAlive;

        if (connChecker != null) {
            connChecker.cancel();
        }

//		if (Logger.BUILD_DEBUG ) {
//			Logger.log(this + ".setKeepAlive( " + keepAlive + " );", true);
//		}

        long period = (keepAlive * 300);

        if (period < 5000) {
            period = 5000;
        }

        connChecker = new ConnectionChecker();
        this.app.getTimer().schedule(connChecker, period, period);


    }

    /**
     * The channel management class of the prtocol layer
     */
    private class ChannelManagementIdToName {

        // It shouldn't consume much memory (something around 2*256 = 512 bytes).
        private String[] _names = new String[256];

        // Without this, the obfuscator failed
        public ChannelManagementIdToName() {
        }

        public void setName(byte channelId, String channelName) {
            if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                Logger.log("ChannelManagementIdToName.setName( " + channelId + ", \"" + channelName + "\" );");
            }
            int iChannelId = (int) channelId;

            // We have to take care of the lame all-signed java types
            if (iChannelId < 0) {
                iChannelId += 128;
            }

            _names[iChannelId] = channelName;
        }

        public String getName(byte channelId) {
            int iChannelId = (int) channelId;

            // We have to take care of the lame all-signed java types
            if (iChannelId < 0) {
                iChannelId += 128;
            }

            String name = _names[iChannelId];

            if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                Logger.log("ChannelManagementIdToName.getName( " + channelId + " ) : \"" + name + "\";");
            }

            return name;
        }
    }

    private class ChannelManagementNameToId {

        private short _counter = 0;
        private Hashtable _nameToId = new Hashtable();

        public short getId(String name) {
            if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                Logger.log("ChannelManagementNameToId.getId( " + name + " );");
            }

            if (_nameToId.containsKey(name)) {
                return ((Short) _nameToId.get(name)).shortValue();
            } else {
                short v = ++_counter;
                _nameToId.put(name, new Short(v));
                sendNamedChannel(name, (byte) v);
                return v;
            }
        }
    }
    private NetworkLayer net;
    private ChannelManagementNameToId cmSend;
    private ChannelManagementIdToName cmRecv;

    private void sendNamedChannel(String name, byte bId) {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log("PrtocolLayer.SendNamedChannel( \"" + name + "\", " + bId + " );");
        }
        byte[] bName = name.getBytes();
        byte[] frame = new byte[(bName.length + 3)];
        frame[0] = IProtocolLayer.NET_S_NC_DEF;
        frame[1] = (byte) (frame.length - 2);
        frame[2] = bId;
        System.arraycopy(bName, 0, frame, 3, bName.length);
        net.sendFrameFirst(frame);
    }

    public void receivedFrame(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.ReceivedFrame( " + Bytes.byteArrayToPrettyString(frame));
        }

        switch (frame[0]) {
            case NET_R_ACK_REQUEST:
                treatFrameAckRequest(frame);
                break;

            case NET_R_ACK_RESPONSE:
                treatFrameAckResponse(frame);
                break;

            case NET_R_IDENT_RESULT:
                treatFrameIdentificationResult(frame);
                break;

            case NET_R_NC_DATA:
                treatFrameData(frame);
                break;

            case NET_R_NC_DATA_LARGE:
                treatFrameDataLarge(frame);
                break;

            case NET_R_NC_DEF:
                treatFrameChannelDef(frame);
                break;

            case NET_S_NC_DATAARRAY:
                treatFrameDataArray(frame);
                break;

            case NET_S_NC_DATAARRAY_LARGE:
                treatFrameDataArrayLarge(frame);
                break;
            default:
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("ProtocolLayer.receivedFrame: Unknow frame type: " + frame[0]);
                }
        }
    }
    private M2MPClientImpl app;

    void setAppLayer(M2MPClientImpl appLayer) {
        app = appLayer;
    }

    public void sendData(String channelName, String data) {
        sendData(channelName, data.getBytes());
    }

    public void sendData(String channelName, byte[] data) {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.SendData( \"" + channelName + "\", byte[" + data.length + "] );");
        }

        byte channelId = (byte) cmSend.getId(channelName);
        sendData(channelId, data);
    }

    public void sendData(byte channelId, byte[] data) {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log("ProtocolLayer.SendData( byte, byte[" + data.length + "] );");
        }

        byte[] frame = null;
        int offset = 0;
        if (data.length <= 254) {
            frame = new byte[(data.length + 3)];
            frame[offset++] = IProtocolLayer.NET_S_NC_DATA;
            frame[offset++] = (byte) (data.length + 1);
            frame[offset++] = channelId;
        } else if (data.length <= 65534) {
            frame = new byte[(data.length + 4)];
            frame[offset++] = IProtocolLayer.NET_S_NC_DATA_LARGE;
            Bytes.intTo2Bytes((data.length + 1), frame, offset);
            offset += 2;
            frame[offset++] = channelId;
        } else {
            return;
        }

        System.arraycopy(data, 0, frame, offset, data.length);

        net.sendFrame(frame);
    }

    private static int dataArraySize(byte[][] data) {

        // We will caclulate the total size of the payload
        int size = 0;

        for (int i = 0; i < data.length; ++i) {
            // We need to add the load of each cell
            size += data[i].length;
        }


        // For each cell, we count one byte header plus the channel id
        size += data.length + 1;

        // If we have more than 255 bytes to send...
        if (size > 254) {

            // We have to switch from one byte to two bytes per cell (so we add one byte for each cell)
            size += data.length;
        }

        return size;
    }

    public void sendData(String channelName, Vector data) {
        sendData(channelName, Bytes.stringsToBytes(data));
    }

    public void sendData(String channelName, String[] data) {
        sendData(channelName, Bytes.stringsToBytes(data));
    }

    public void sendData(String channelName, byte[][] data) {
        byte channelId = (byte) cmSend.getId(channelName);
        sendData(channelId, data);
    }

    public void sendData(byte channelId, byte[][] data) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendData( " + channelId + ", byte[" + data.length + "] );");
        }
        // We have 1 byte for the type
        int size = dataArraySize(data);
        byte[] frame = null;
        int offset = 0;
        if (size <= 254) {
            frame = new byte[(size + 2)];
            frame[offset++] = IProtocolLayer.NET_S_NC_DATAARRAY;
            //frame[offset++] = (byte) size;
            Bytes.intTo1Byte(size, frame, offset++);
            frame[offset++] = channelId;

            for (int i = 0; i < data.length; ++i) {
                byte[] subData = data[i];
                Bytes.intTo1Byte(subData.length, frame, offset++);
                System.arraycopy(subData, 0, frame, offset, subData.length);
                offset += subData.length;
            }
        } else if (size <= 65534) {
//			if (Logger.BUILD_DEBUG) {
//				Logger.log("m2mp.sendData: size=" + size);
//			}
            frame = new byte[(size + 3)];
            frame[offset++] = IProtocolLayer.NET_S_NC_DATAARRAY_LARGE;
            Bytes.intTo2Bytes(size, frame, offset);
            offset += 2;
            frame[offset++] = channelId;

            for (int i = 0; i < data.length; ++i) {
//				if (Logger.BUILD_DEBUG) {
//					Logger.log("m2mp.sendData: i=" + i);
//				}
                byte[] subData = data[i];
                Bytes.intTo2Bytes(subData.length, frame, offset);
                offset += 2;
                System.arraycopy(subData, 0, frame, offset, subData.length);
                offset += subData.length;
            }
        } else // We won't handle more as it would be absurd on a such little equipment
        {
            return;
        }

//		if (Logger.BUILD_DEBUG) {
//			Logger.log("m2mp.sendData: offset=" + offset);
//		}

        net.sendFrame(frame);
    }

    public void disconnected() {
        cmSend = new ChannelManagementNameToId();
        cmRecv = new ChannelManagementIdToName();
        if (app != null) {
            app.onDisconnected();
        }
    }

    public long getLastRecvTime() {
        return net.getLastRecvTime();
    }

    public void updateLastRecvTime() {
        net.updateLastRecvTime();
    }

    public long getLastSendTime() {
        return net.getLastSendTime();
    }
}
