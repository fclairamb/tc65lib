package org.javacint.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.javacint.at.ATCommands;
import org.javacint.logging.Logger;
import org.javacint.utilities.Hex;

public abstract class ATSerialProtocol extends Connection {

    private static final boolean DEBUG = false;
	private Connection atDataConnection;
    protected ATSerialProfile atSerialProfile;
    public static final char START_TRANSFER_MESSAGE = '<';
    public static final char STOP_TRANSFER_MESSAGE = '>';
    public static final char CHANNEL_CLOSE = '#';
    public static final char START_RESPONSE_MESSAGE = '{';
    public static final char STOP_RESPONSE_MESSAGE = '}';
    public static final char PROTOCOL_ERROR = '!';
    public static final char TRANSMISSION_OK = '+';
    public static final char TRANSMISSION_ERROR = '-';
    public static final short I2C_MAX_LENGTH = 512;
    public static final byte ERROR_LENGTH = 4;
    public static final byte SPI_READ_OFFSET = 2;
    public static final byte SPI_READ_LENGTH = 4;
    private byte messageID = 0;
    private StringBuffer buffer = new StringBuffer();
    private int lastReadByte;

    public boolean open(ConnectionProfile paramConnectionProfile) {
        if ((paramConnectionProfile instanceof ATSerialProfile)) {
            this.atSerialProfile = ((ATSerialProfile) paramConnectionProfile);
        } else {
            return false;
        }
        try {
            ATCommands.sendr("AT^SCFG=\"MeOpMode/MipsMa\",\"off\"");

            this.atDataConnection = ATCommands.getATDataConnection();
            if (this.atDataConnection.open(this.atSerialProfile)) {
                this.is = this.atDataConnection.getInputStream();
                this.os = this.atDataConnection.getOutputStream();
                return true;
            }
        } catch (Exception e) {
            close();
            Logger.log(e.getMessage(), getClass());
        }
        return false;
    }

    public void close() {
        try {
            this.os.write("#".getBytes());
        } catch (Exception localException2) {
            Logger.log(localException2.getMessage(), getClass());
        }
        super.close();
        if (this.atDataConnection != null) {
            this.atDataConnection.close();
            this.atDataConnection = null;
        }
    }

    protected final byte[] readData() throws IOException {
        while (((this.lastReadByte = this.is.read()) != 123) && (this.is.available() > 0)) {
            if (DEBUG) {
                Logger.log("Read before:" + ((char) lastReadByte), this.getClass());
            }
        }
        if (DEBUG) {
            Logger.log("Read:" + new String(new byte[]{(byte) lastReadByte}), this.getClass());
        }
        while (this.is.available() > 0) {
            this.lastReadByte = this.is.read();
            if (DEBUG) {
                Logger.log("Read:" + new String(new byte[]{(byte) lastReadByte}), this.getClass());
            }
            if (this.lastReadByte == 125) {
                String str = this.buffer.toString();
                if (DEBUG) {
                    Logger.log("R." + str + ".", getClass());
                }
                this.buffer.setLength(0);
                return Hex.doHexBytesArray(str.substring(str.indexOf(TRANSMISSION_OK) + 1));
            }
            if ((this.lastReadByte == 33) || (this.lastReadByte == 45)) {
                throw new IOException("ATserialProtocol: protocol error");
            }
            this.buffer.append((char) this.lastReadByte);
        }
        return new byte[0];
    }

    protected final void sendData(byte[] paramArrayOfByte) throws IOException {
        this.messageID = (byte) ((this.messageID + 1) & 0x0F);
        this.os.write(START_TRANSFER_MESSAGE);
        this.os.write(this.messageID + 65);
        this.os.write(Hex.doHexCharsArray(paramArrayOfByte).getBytes());
        this.os.write(STOP_TRANSFER_MESSAGE);
        if (DEBUG) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(START_TRANSFER_MESSAGE);
            baos.write(this.messageID + 65);
            baos.write(Hex.doHexCharsArray(paramArrayOfByte).getBytes());
            baos.write(STOP_TRANSFER_MESSAGE);
            Logger.log("W." + baos.toString() + ".", getClass());
        }
        this.os.flush();
    }

    /*
     public static void setDebug(boolean paramBoolean) {
     jdField_a_of_type_Boolean = paramBoolean;
     }
     *
     */
}
