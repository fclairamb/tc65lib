package org.javacint.io;

public class SPIProfile extends ATSerialProfile
{
  public static final int BUS_SPEED_SPI_100kbs = 0;
  public static final int BUS_SPEED_SPI_250kbs = 1;
  public static final int BUS_SPEED_SPI_500kbs = 2;
  public static final int BUS_SPEED_SPI_1083kbs = 3;
  public static final int BUS_SPEED_SPI_3250kbs = 4;
  public static final int BUS_SPEED_SPI_6500kbs = 5;
  public static final int SPI_MODE_0 = 0;
  public static final int SPI_MODE_1 = 1;
  public static final int SPI_MODE_2 = 2;
  public static final int SPI_MODE_3 = 3;
  private int spiMode = 0;

  public String getProfile()
  {
    return "at^sspi=10" + this.baudrate + "0,0000,0000,0000," + this.spiMode + "000";
  }

  public void setSPIMode(int spiMode)
  {
    if (spiMode > 3)
      return;
    this.spiMode = spiMode;
  }

  public int getSPIMode()
  {
    return this.spiMode;
  }
}