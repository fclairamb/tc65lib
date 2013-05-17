package org.javacint.io;

import java.io.IOException;
import java.io.OutputStream;

final class spiConnectionOutputStream extends OutputStream
{
  private final SPIConnection spiConnection;

  public void write(int paramInt) throws IOException
  {
    byte[] byteArr = new byte[] { (byte)paramInt };
    write(byteArr);
  }

  public void write(byte[] paramArrayOfByte) throws IOException
  {
    SPIConnection.readToBuffer(this.spiConnection, this.spiConnection.transferMessage(0, this.spiConnection.atSerialProfile.INPUT_BUFFER_SIZE, paramArrayOfByte));
    SPIConnection.setReadMarker(this.spiConnection, 0);
  }

  spiConnectionOutputStream(SPIConnection paramSPIConnection)
  {
    this.spiConnection = paramSPIConnection;
  }
}

/* Location:           C:\Documents and Settings\PyTh0n\libX700\lib\libX700.jar
 * Qualified Name:     io.i
 * JD-Core Version:    0.6.0
 */