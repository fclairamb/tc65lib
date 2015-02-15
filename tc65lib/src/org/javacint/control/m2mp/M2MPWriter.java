package org.javacint.control.m2mp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import org.javacint.common.Bytes;
import org.javacint.control.m2mp.data.AcknowledgeRequest;
import org.javacint.control.m2mp.data.AcknowledgeResponse;
import org.javacint.control.m2mp.data.IdentificationRequest;
import org.javacint.control.m2mp.data.Message;
import org.javacint.control.m2mp.data.NamedData;
import org.javacint.control.m2mp.data.NamedDataArray;
import org.javacint.logging.Logger;

public class M2MPWriter {

    private final ChannelManagementNameToId cm = new ChannelManagementNameToId();
    private final OutputStream os;

    public M2MPWriter(OutputStream os) {
        this.os = os;
    }

    private class ChannelManagementNameToId {

        private short counter = 0;
        private Hashtable nameToId = new Hashtable();

        public short getId(String name) throws IOException {
            if (nameToId.containsKey(name)) {
                return ((Short) nameToId.get(name)).shortValue();
            } else {
                short v = ++counter;
                if (v >= 255) {
                    nameToId.clear();
                }

                if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                    Logger.log(this + ".getId( " + name + " ): " + v);
                }

                nameToId.put(name, new Short(v));
                sendNamedChannel(name, (byte) v);
                return v;
            }
        }

        public String toString() {
            return "ChannelManagementNameToId";
        }
    }

    public void write(Message m) throws IOException {
        if (m instanceof NamedData) {
            NamedData msg = (NamedData) m;
            sendData(msg.name, msg.data);
        } else if (m instanceof NamedDataArray) {
            NamedDataArray msg = (NamedDataArray) m;
            sendData(msg.name, msg.data);
        } else if (m instanceof AcknowledgeRequest) {
            AcknowledgeRequest msg = (AcknowledgeRequest) m;
            sendAckRequest(msg.nb);
        } else if (m instanceof AcknowledgeResponse) {
            AcknowledgeResponse msg = (AcknowledgeResponse) m;
            sendAckResponse(msg.nb);
        } else if (m instanceof IdentificationRequest) {
            IdentificationRequest msg = (IdentificationRequest) m;
            sendIdentificationRequest(msg);
        } else {
            throw new RuntimeException(this + ".write: Could not handle message " + m);
        }
    }

    private void sendAckRequest(byte b) throws IOException {
        os.write(new byte[]{FrameType.S_ACK_REQUEST, b});
    }

    private void sendAckResponse(byte b) throws IOException {
        os.write(new byte[]{FrameType.S_ACK_RESPONSE, b});
    }

    private void sendData(String channelName, String data) throws IOException {
        sendData(channelName, data.getBytes());
    }

    private void sendData(String channelName, byte[] data) throws IOException {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendData( \"" + channelName + "\", byte[" + data.length + "] );");
        }

        byte channelId = (byte) cm.getId(channelName);
        sendData(channelId, data);
    }

    private void sendData(byte channelId, byte[] data) throws IOException {
        /*
         if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
         Logger.log(this + ".sendData( " + channelId + ", byte[" + data.length + "] );");
         }
         */

        byte[] frame;
        int offset = 0;
        if (data.length <= 254) {
            frame = new byte[(data.length + 3)];
            frame[offset++] = FrameType.S_NC_DATA;
            frame[offset++] = (byte) (data.length + 1);
            frame[offset++] = channelId;
        } else if (data.length <= 65534) {
            frame = new byte[(data.length + 4)];
            frame[offset++] = FrameType.S_NC_DATA_LARGE;
            Bytes.intTo2Bytes((data.length + 1), frame, offset);
            offset += 2;
            frame[offset++] = channelId;
        } else {
            return;
        }

        System.arraycopy(data, 0, frame, offset, data.length);

        os.write(frame);
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

    private void sendData(String channelName, byte[][] data) throws IOException {
        byte channelId = (byte) cm.getId(channelName);
        sendData(channelId, data);
    }

    private void sendData(byte channelId, byte[][] data) throws IOException {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendData( " + channelId + ", byte[" + data.length + "] );");
        }
        // We have 1 byte for the type
        int size = dataArraySize(data);
        byte[] frame;
        int offset = 0;
        if (size <= 254) {
            frame = new byte[(size + 2)];
            frame[offset++] = FrameType.S_NC_DATAARRAY;
            Bytes.intTo1Byte(size, frame, offset++);
            frame[offset++] = channelId;

            for (int i = 0; i < data.length; ++i) {
                byte[] subData = data[i];
                Bytes.intTo1Byte(subData.length, frame, offset++);
                System.arraycopy(subData, 0, frame, offset, subData.length);
                offset += subData.length;
            }
        } else if (size <= 65534) {
            frame = new byte[(size + 3)];
            frame[offset++] = FrameType.S_NC_DATAARRAY_LARGE;
            Bytes.intTo2Bytes(size, frame, offset);
            offset += 2;
            frame[offset++] = channelId;

            for (int i = 0; i < data.length; ++i) {
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

        os.write(frame);
    }

    private void sendNamedChannel(String name, byte bId) throws IOException {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".sendNamedChannel( \"" + name + "\", " + bId + " );");
        }
        byte[] bName = name.getBytes();
        byte[] frame = new byte[(bName.length + 3)];
        frame[0] = FrameType.S_NC_DEF;
        frame[1] = (byte) (frame.length - 2);
        frame[2] = bId;
        System.arraycopy(bName, 0, frame, 3, bName.length);
        os.write(frame);
    }

    private void sendIdentificationRequest(IdentificationRequest msg) throws IOException {
        byte[] rawId = msg.ident.getBytes();
        byte[] frame = new byte[(rawId.length + 2)];
        frame[0] = FrameType.S_IDENT;
        frame[1] = (byte) rawId.length;
        System.arraycopy(rawId, 0, frame, 2, rawId.length);
        os.write(frame);
    }

    public void close() throws IOException {
        os.close();
    }

    public String toString() {
        return "M2MPWriter";
    }
}
