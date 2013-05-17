package org.javacint.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SPIConnection extends ATSerialProtocol {

    private spiConnectionInputStream inputStream;
    private spiConnectionOutputStream outputStream;
    private byte[] inputBuffer;
    private int readMarker = 0;

    public synchronized byte[] transferMessage(int readOffset, int readLength, byte[] paramArrayOfByte) throws IOException {
        byte[] arrayOfByte = new byte[paramArrayOfByte.length + 3];
        arrayOfByte[0] = (byte) readOffset;
        arrayOfByte[1] = (byte) (readLength >> 8);
        arrayOfByte[2] = (byte) readLength;
        System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 3, paramArrayOfByte.length);
        sendData(arrayOfByte);
        return readData();
    }

    public void transferMessage(byte[] paramArrayOfByte) throws IOException {
        transferMessage(0, 0, paramArrayOfByte);
    }

    public InputStream getInputStream() {
        if (this.inputStream == null) {
            this.inputBuffer = new byte[1];
            this.inputStream = new spiConnectionInputStream(this);
        }
        this.readMarker = this.atSerialProfile.INPUT_BUFFER_SIZE;
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        if (this.outputStream == null) {
            this.outputStream = new spiConnectionOutputStream(this);
        }
        return this.outputStream;
    }

    static byte[] getInputBuffer(SPIConnection paramSPIConnection) {
        return paramSPIConnection.inputBuffer;
    }

    static int getAndAdvanceReadMarker(SPIConnection paramSPIConnection) {
        return paramSPIConnection.readMarker++;
    }

    static int getReadMarker(SPIConnection paramSPIConnection) {
        return paramSPIConnection.readMarker;
    }

    static byte[] readToBuffer(SPIConnection paramSPIConnection, byte[] paramArrayOfByte) {
        return paramSPIConnection.inputBuffer = paramArrayOfByte;
    }

    static int setReadMarker(SPIConnection paramSPIConnection, int paramInt) {
        return paramSPIConnection.readMarker = paramInt;
    }
}
