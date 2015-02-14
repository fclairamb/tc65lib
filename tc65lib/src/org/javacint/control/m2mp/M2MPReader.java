package org.javacint.control.m2mp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.javacint.common.Bytes;
import org.javacint.control.m2mp.data.AcknowledgeRequest;
import org.javacint.control.m2mp.data.AcknowledgeResponse;
import org.javacint.control.m2mp.data.IdentificationResponse;
import org.javacint.control.m2mp.data.Message;
import org.javacint.control.m2mp.data.NamedData;
import org.javacint.control.m2mp.data.NamedDataArray;
import org.javacint.logging.Logger;

public class M2MPReader {

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
                Logger.log(this + ".setName( " + channelId + ", \"" + channelName + "\" );");
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
                Logger.log(this + ".getName( " + channelId + " ) : \"" + name + "\";");
            }

            if (name == null) {
                Logger.log("M2MP: Channel: Could not find a name for " + iChannelId, true);
            }

            return name;
        }

        public String toString() {
            return "ChannelManagementIdToName";
        }
    }
    // We have no reason to reset this...
    private final ChannelManagementIdToName channelManagement = new ChannelManagementIdToName();
    private M2MPEventsListener app;
    private final InputStream is;

    public M2MPReader(InputStream is, M2MPEventsListener listener) {
        this.is = is;
        this.app = listener;
    }

    public Message treatFrame(byte[] frame) throws IOException {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".receivedFrame( " + Bytes.byteArrayToPrettyString(frame));
        }

        switch (frame[0]) {
            case FrameType.R_ACK_REQUEST:
                return treatFrameAckRequest(frame);

            case FrameType.R_ACK_RESPONSE:
                return treatFrameAckResponse(frame);

            case FrameType.R_IDENT_RESULT:
                return treatFrameIdentificationResult(frame);

            case FrameType.R_NC_DATA:
                return treatFrameData(frame);

            case FrameType.R_NC_DATA_LARGE:
                return treatFrameDataLarge(frame);

            case FrameType.R_NC_DEF:
                treatFrameChannelDef(frame);
                return null;

            case FrameType.S_NC_DATAARRAY:
                return treatFrameDataArray(frame);

            case FrameType.S_NC_DATAARRAY_LARGE:
                return treatFrameDataArrayLarge(frame);

            default:
                throw new IOException("M2MPReader.read: Invalid frame type " + frame[0]);
        }
    }
    private int nbUnrepliedAcks;

    private AcknowledgeRequest treatFrameAckRequest(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameAckRequest( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        //app.onReceivedAckRequest(frame[1]);
        nbUnrepliedAcks = 0;
        return new AcknowledgeRequest(frame[1]);
    }

    private AcknowledgeResponse treatFrameAckResponse(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameAckResponse( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        //app.onReceivedAckResponse(frame[1]);
        nbUnrepliedAcks = 0;
        return new AcknowledgeResponse(frame[1]);
    }

    private void treatFrameChannelDef(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameChannelDef( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }
        /*
         * Bytes are : =========== 0 : Type 1 : Size 2 : Channel id 3-X :
         * Channel name
         */
        byte channelId = frame[2];

        String channelName = new String(frame, 3, frame.length - 3);

        channelManagement.setName(channelId, channelName);
    }

    private NamedData treatFrameData(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameData( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1 : Size 2 : Channel Id 3-X : Data
         */
        byte channelId = frame[2];
        String channelName = channelManagement.getName(channelId);

        byte[] data = new byte[(frame.length - 3)];
        System.arraycopy(frame, 3, data, 0, data.length);

        //app.onReceivedData(channelName, data);
        return new NamedData(channelName, data);
    }

    private NamedData treatFrameDataLarge(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameDataLarge( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1-2 : Size 3 : Channel Id 4-X : Data
         */

        byte channelId = frame[3];
        String channelName = channelManagement.getName(channelId);

        byte[] data = new byte[(frame.length - 4)];
        System.arraycopy(frame, 4, data, 0, data.length);

        //app.onReceivedData(channelName, data);
        return new NamedData(channelName, data);
    }

    private NamedDataArray treatFrameDataArray(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameDataArray( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1 : Size 2 : Channel Id 3-X : Data
         */

        // first byte for the size
        int off = 2;

        byte channelId = frame[off++];
        String channelName = channelManagement.getName(channelId);

        Vector content = new Vector();

        while (off < frame.length) {
            int size = frame[off++];
            byte[] subData = new byte[size];
            System.arraycopy(frame, off, subData, 0, size);
            off += size;
            content.addElement(subData);
        }

        byte[][] data = new byte[content.size()][];
        content.copyInto(data);

        //app.onReceivedData(channelName, data);
        return new NamedDataArray(channelName, data);
    }

    private NamedDataArray treatFrameDataArrayLarge(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameDataArrayLarge( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        /*
         * Bytes are : 0 : Type 1-2 : Size 3 : Channel Id 4-X : Data
         */
        int off = 3;

        byte channelId = frame[off++];
        String channelName = channelManagement.getName(channelId);

        Vector content = new Vector();


        while (off < frame.length) {
            int size = Bytes.bytesToShort(frame, off);
            off += 2;

            byte[] subData = new byte[size];
            System.arraycopy(frame, off, subData, 0, size);
            off += size;

            content.addElement(subData);
        }

        byte[][] data = new byte[content.size()][];
        content.copyInto(data);

        //app.onReceivedData(channelName, data);
        return new NamedDataArray(channelName, data);
    }

    private IdentificationResponse treatFrameIdentificationResult(byte[] frame) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".treatFrameIdentificationResult( " + Bytes.byteArrayToPrettyString(frame) + " );");
        }

        boolean identified = frame[1] == 0x01;
        //app.onReceivedIdentificationResult(identified);
        return new IdentificationResponse(identified);
    }
    private int offset = 0;
    private byte[] frame;
    private int type = 0;
    private byte[] header = new byte[3];

    public Message read() throws IOException {
        while (true) {
            // If we don't yet have a packet
            if (frame == null) {
                if (offset == 0) {
                    type = is.read();
                    header[offset++] = (byte) type;
                    if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                        Logger.log("_type = " + type + " / " + header[0]);
                    }

                    if (type < FrameType.MSG_SPECIALIZED_MAX && (type == FrameType.R_IDENT_RESULT
                            || type == FrameType.R_ACK_REQUEST
                            || type == FrameType.R_ACK_RESPONSE)) {
                        frame = new byte[2];
                        frame[0] = header[0];
                    }
                } else {
                    int b = is.read();

                    if (b == -1) {
                        throw new IOException("M2MPReader.read: We got disconnected !");
                    }

                    header[offset++] = (byte) b;

//						if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog) {
//							Logger.log("tcpIn=" + b + " / _offset=" + _offset);
//						}

                    // If it's a one-byte sized message
                    if (offset == 2 && type < FrameType.NET_MSG_1BYTESIZED_MAX) {
//							if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog) {
//								Logger.log("_type = " + _type + " < " + IProtocolLayer.NET_MSG_1BYTESIZED_MAX);
//							}

                        int size = Bytes.byteToInt(header[1]) + 2;
                        frame = new byte[size];
                        System.arraycopy(header, 0, frame, 0, 2);

                        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                            Logger.log(this + ".run: size=" + size);
                        }


                    } // If it's two-bytes sized message
                    else if (offset == 3 && type < FrameType.NET_MSG_2BYTESSIZED_MAX) {
                        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                            Logger.log("_type = " + type + " < " + FrameType.NET_MSG_2BYTESSIZED_MAX);
                        }

                        int size = Bytes.bytesToShort(header, 1) + 3;
                        frame = new byte[size];
                        System.arraycopy(header, 0, frame, 0, 3);

                        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                            Logger.log("NetworkLayer.NetworkReceive.Work : size=" + size);
                        }
                    }
                }
            } else {
                // We try to read as much as possible
//					if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog) {
//						Logger.log("_offset=" + _offset + " / _frame.length=" + _frame.length);
//					}
                offset += is.read(frame, offset, frame.length - offset);
//					if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog) {
//						Logger.log("_offset=" + _offset + " / _frame.length=" + _frame.length);
//					}
            }
            // Could be used if we have zero sized messages
            if (frame != null && offset == frame.length) {
                try {
                    return treatFrame(frame);
                } finally {
                    resetFrame();
                }
            }
        }
    }

    private void resetFrame() {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".resetFrame();");
        }
        offset = 0;
        frame = null;
    }
}
