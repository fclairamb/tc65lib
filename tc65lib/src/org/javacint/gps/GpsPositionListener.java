package org.javacint.gps;

/**
 * GPS Position listener
 */
public interface GpsPositionListener {
	
	public static final String TYPE_GPS_CHIP_VERSION = "version";
	public static final String TYPE_GPS_CHIP_VERSION2 = "version2";
	
	/**
	 * GPS Position received
	 * @param pos Position received
	 */
	public void positionReceived( GpsPosition pos );
	
        /**
         * Additionnal data received.
         * @param type Type of data received
         * @param value Content of the data received
         */
	public void positionAdditionnalReceived( String type, String value );
}
