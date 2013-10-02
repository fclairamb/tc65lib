package org.javacint.i2c;

import com.siemens.icm.io.I2cBusConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import org.javacint.common.Bytes;
import org.javacint.logging.Logger;

/**
 * I2C management code
 */
public class I2CConnector {

    private int connString_baudRate = 400;
    private String connString_readDelay = "0";
    private String connString_writeDelay = "0";
    private int i2c_messageIdCounter = 0;
    private I2cBusConnection i2c_connection;
    private InputStream i2c_is;
    private OutputStream i2c_os;
    private static final boolean log = true;

    /**
     * Protocol error occured
     */
    public class ProtocolErrorException extends RuntimeException {

        private final byte msgId;
        private final int byteNb;

        /**
         * Default constructor
         *
         * @param msgId message id
         * @param nb byte's number
         */
        public ProtocolErrorException(byte msgId, int nb) {
            this.msgId = msgId;
            this.byteNb = nb;
        }

        public String getMessage() {
            return "Protocol transmission error on the " + byteNb + "th byte for message " + (char) msgId + "!";
        }
    }

    /**
     * Protocol NACK exception occured
     */
    public class ProtocolNackException extends RuntimeException {

        private final byte msgId;
        private final int byteNb;

        /**
         * Default cosntructor
         *
         * @param msgId message id
         * @param nb byte's number
         */
        public ProtocolNackException(byte msgId, int nb) {
            this.msgId = msgId;
            this.byteNb = nb;
        }

        public String getMessage() {
            return "Protocol NACK error on the " + byteNb + "th byte for message " + (char) msgId + "!";
        }
    }

    /**
     * Messages id don't match error
     */
    public class MessagesIdDontMatchException extends RuntimeException {

        private final byte msgSentId;
        private final byte msgReadId;

        /**
         * Default constructor
         *
         * @param msgSentId Sent message id
         * @param msgReadId Read message id
         */
        public MessagesIdDontMatchException(byte msgSentId, byte msgReadId) {
            this.msgSentId = msgSentId;
            this.msgReadId = msgReadId;
        }

        public String getMessage() {
            return "Protocols don't match between " + (char) msgSentId + " and " + (char) msgReadId + " !";
        }
    }

    /**
     * Default constructor
     */
    public I2CConnector() {
    }

    private void open() throws IOException {
        if (i2c_connection == null) {
            String connectionString = "i2c:0;baudrate=" + connString_baudRate;
            if (!connString_readDelay.equals("0")) {
                connectionString += ";readDelay=" + connString_readDelay;
            }
            if (!connString_writeDelay.equals("0")) {
                connectionString += ";writeDelay=" + connString_writeDelay;
            }

            if (Logger.BUILD_DEBUG && log) {
                Logger.log("connectionString=\"" + connectionString + "\"");
            }
            i2c_connection = (I2cBusConnection) Connector.open(connectionString);
            i2c_is = i2c_connection.openInputStream();
            i2c_os = i2c_connection.openOutputStream();
        }
    }

    private void close() throws IOException {
        if (i2c_connection != null) {
            i2c_is.close();
            i2c_os.close();
            i2c_connection.close();
            i2c_connection = null;
        }
    }

    private String readFrame() throws Exception {
        open();
        StringBuffer sb = new StringBuffer();

        // Worst case scenario we spend 300ms
        for (int i = 0; i < 30; i++) {
            while (i2c_is.available() > 0) {
                int b = i2c_is.read();
                sb.append((char) b);
                if ((char) b == '}') {
                    if (Logger.BUILD_DEBUG && log) {
                        Logger.log("i2c.readFrame: " + sb.toString());
                    }
                    return sb.toString();
                }
            }
            if (Logger.BUILD_DEBUG && log) {
                Logger.log("Nothing received yet (" + i + ") !");
            }
            Thread.sleep(10);
        }
        throw new RuntimeException("Could not read frame !");
    }

    private byte[] treatFrame(String str, byte sentMessageId) {
        if (Logger.BUILD_NOTICE && log) {
            Logger.log("i2c.treatFrame( \"" + str + "\", " + sentMessageId + " );");
        }

        byte msgId = (byte) Integer.parseInt(str.substring(1, 2), 16);

        if (sentMessageId != msgId) {
            throw new MessagesIdDontMatchException(sentMessageId, msgId);
        }

        char code = str.charAt(2);

        if (code == '+') {
            if (str.length() == 4) {
                return null;
            } else {
                String content = str.substring(3, str.length() - 1);
                if (Logger.BUILD_NOTICE && log) {
                    Logger.log("i2c.read: Message " + msgId + " was received: " + content);
                }
                return Bytes.hexStringToByteArray(content);
            }
        } else if (code == '-') {
            int xthbyte = Integer.parseInt(str.substring(3, 7), 16);
            throw new ProtocolNackException((byte) msgId, xthbyte);
        } else if (code == '!') {
            int xthbyte = Integer.parseInt(str.substring(3, 7), 16);
            throw new ProtocolErrorException((byte) msgId, xthbyte);
        } else {
            throw new RuntimeException("Code " + (char) code + " isn't supported !");
        }
    }

    private void sendFrame(String data) throws IOException {
        if (Logger.BUILD_NOTICE && log) {
            Logger.log(this + ".sendFrame( \"" + data + "\" );");
        }
        open();
        i2c_os.write(data.getBytes());
        i2c_os.flush();
    }

    private byte getMessageId() {
        return (byte) (i2c_messageIdCounter++ % 16);
    }

    /**
     * Write a message
     *
     * @param slaveId slave to write the message to
     * @param data data to send
     * @throws Exception
     */
    public void write(int slaveId, byte[] data) throws Exception {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".write( " + slaveId + ", " + Bytes.
                    byteArrayToPrettyString(data) + " )");
        }
        byte msgId = getMessageId();

        slaveId = slaveId * 2; // We convert it to the write address

        StringBuffer sb = new StringBuffer();
        sb.append("<").
                append(Integer.toHexString(msgId));
        Bytes.byteArrayToHexString(sb, new byte[]{(byte) (slaveId & 0xff)});
        Bytes.byteArrayToHexString(sb, data);
        sb.append(">");

        sendFrame(sb.toString());
        treatFrame(readFrame(), msgId);
    }

    public void write(int slaveid, byte data) throws Exception {
        write(slaveid, new byte[]{data});
    }

    /**
     * Read a message
     *
     * @param slaveId slave to read the message from
     * @param size number of bytes to read
     * @return Read bytes
     * @throws Exception
     */
    public byte[] read(int slaveId, int size) throws Exception {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".read( " + slaveId + ", " + size + " );");
        }
        byte msgId = getMessageId();

        slaveId = slaveId * 2 + 1; // We convert it to the read address

        StringBuffer sb = new StringBuffer();
        sb.append("<").
                append(Integer.toHexString(msgId).
                toUpperCase());
        Bytes.byteArrayToHexString(sb, new byte[]{(byte) (slaveId & 0xff)});
        String readSize = Integer.toHexString(size).
                toUpperCase();
        while (readSize.length() < 4) {
            readSize = "0" + readSize;
        }
        sb.append(readSize);
        sb.append(">");

        sendFrame(sb.toString());
        return treatFrame(readFrame(), msgId);
    }

    public byte[] get(int slaveId, byte register, int size) throws Exception {
        write(slaveId, new byte[]{register});
        return read(slaveId, size);
    }

    public byte get(int slaveId, byte register) throws Exception {
        return get(slaveId, register, 1)[0];
    }

    /**
     * Set the baudrate of the I2C Bus
     *
     * @param baudrate baudrate (can be 100 to 400 kbps)
     * @throws IOException
     */
    public void setBaudrate(int baudrate) throws IOException {
        this.connString_baudRate = baudrate;
        close();
    }

    /**
     * Set the read delay of the I2C bus
     *
     * @param readDelay Read delay
     */
    public void setReadDelay(String readDelay) throws IOException {
        this.connString_readDelay = readDelay;
        close();
    }

    /**
     * Set the write delay of the I2C bus
     *
     * @param writeDelay Write delay
     * @throws IOException
     */
    public void setWriteDelay(String writeDelay) throws IOException {
        this.connString_writeDelay = writeDelay;
        close();
    }
}
