package org.javacint.io;

import java.io.InputStream;

final class spiConnectionInputStream extends InputStream {

    private final SPIConnection spiConnection;

    public int read() {
        return SPIConnection.getInputBuffer(this.spiConnection)[SPIConnection.getAndAdvanceReadMarker(this.spiConnection)] & 0xFF;
    }

    public int available() {
        return this.spiConnection.atSerialProfile.INPUT_BUFFER_SIZE - SPIConnection.getReadMarker(this.spiConnection);
    }

    spiConnectionInputStream(SPIConnection paramSPIConnection) {
        this.spiConnection = paramSPIConnection;
    }
}

/* Location:           C:\Documents and Settings\PyTh0n\libX700\lib\libX700.jar
 * Qualified Name:     io.f
 * JD-Core Version:    0.6.0
 */
