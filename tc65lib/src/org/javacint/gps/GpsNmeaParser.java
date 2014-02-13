package org.javacint.gps;

import java.io.*;
import javax.microedition.io.*;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 * GPS NMEA parser.
 *
 * GPS NMEA protocol parsing.
 */
public class GpsNmeaParser implements Runnable {

    /**
     * Position object. This object is never changed to avoid having to
     * create/collect new instances. If it is used outside the main thread, it
     * must be cloned.
     */
    private final GpsPosition pos = new GpsPosition();
    /**
     * Serial communication
     */
    private CommConnection comm = null;
    /**
     * Input stream
     */
    private InputStream commIs = null;
    /**
     * Output stream
     */
    private OutputStream commOs = null;
    /**
     * Buffer to read NMEA sentences
     */
    private final StringBuffer buff = new StringBuffer();
    /**
     * Temporary number of satellites
     */
    private int tempNbStats = 0;
    /**
     * Number of correct sentences
     */
    private int nbOkSentences = 0;
    /**
     * Serial port number
     */
    private final int portNumber;
    /**
     * Serial port speed
     */
    private final int portSpeed;
    /**
     * Thread
     */
    private final Thread thread = new Thread(this, "gps");
    /**
     * Event listener
     */
    private final GpsPositionListener listener;
    private final int NB_FRAMES_TO_SHOW = 5;

    /**
     * Get the number of correct NMEA sentences
     *
     * @return Number of correct NMEA sentences
     */
    public int getNbSentences() {
        return nbOkSentences;
    }

    /**
     * NMEA GPS Parser constructor
     *
     * @param listener Listener of received positions
     * @param portNumber Port number (0 for ASC0, 1 for ASC1, -1 for XT65
     * internal GPS)
     * @param portSpeed Port speed
     */
    public GpsNmeaParser(GpsPositionListener listener, int portNumber, int portSpeed) {
//		_atc = atc;
        this.listener = listener;
        this.portNumber = portNumber;
        this.portSpeed = portSpeed;

        if (Logger.BUILD_DEBUG) {
            Logger.log("GpsNmeaParser.init( " + portNumber + ", " + portSpeed + " );", true);
        }

        if (pos.date == null) {
            pos.date = "null";
        }
    }

    /**
     * Used to get current position
     *
     * @return Current position
     */
    public synchronized GpsPosition getPos() {
        return new GpsPosition(pos);
    }

    /**
     * Converts from degree to decimal values
     *
     * @param deg Degree value
     * @return Decimal value
     */
    private static double DegToDec(double deg) {
        // Really not pretty code, but it should be fast and working
        double dec;
        dec = (double) ((long) (deg / 100));
        dec += ((double) ((long) (deg % 100))) / 60;
        dec += ((double) ((long) ((deg * 100) % 100))) / 6000;
        dec += ((double) ((long) ((deg * 10000) % 100))) / 600000;
        dec += ((double) ((long) ((deg * 1000000) % 100))) / 60000000;

        // We cut
        dec = ((double) ((long) (dec * 1000000))) / 1000000;

        return dec;
    }

    /**
     * Builds a "bogus time" to quickly compare times
     *
     * @param s String to build the bogus time
     * @return Bogus time
     */
    private static int timeToBtime(String s) {
        try {
            return Integer.parseInt(s.substring(0, 2)) * 3600
                    + Integer.parseInt(s.substring(2, 4)) * 60
                    + Integer.parseInt(s.substring(4, 6));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Parse the NMEA sentences
     *
     * @param nmea NMEA sentence string
     * @return If a position was found
     */
    private boolean parseNMEA(String nmea) {
//        if (Logger.BUILD_DEBUG) {
//            Logger.log("GpsNmeaParser.parseNMEA( \"" + sNMEA + "\" );", true);
//        }

        // We cut the arguments of the NMEA sentence
        String[] aNMEA = Strings.split(',', nmea.substring(0, (nmea.length() - 3)));
        /*
         * Example:
         * $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70
         */

        if (aNMEA[0].compareTo("GPRMC") == 0) {
            try {
                // Latitude
                double lat = aNMEA[3].length() > 0 ? DegToDec(Double.parseDouble(aNMEA[3])) : -255;
                if (aNMEA[4].compareTo("S") == 0) {
                    lat *= -1;
                }

                // Lontitude
                double lon = aNMEA[5].length() > 0 ? DegToDec(Double.parseDouble(aNMEA[5])) : -255;
                if (aNMEA[6].compareTo("W") == 0) {
                    lon *= -1;
                }

                // Speed
                double spd;
                // Speed can be omitted by some chips
                if (aNMEA[7].length() > 0) {
                    spd = (double) (long) (Double.parseDouble(aNMEA[7]) * 1.852 * 10);
                    spd /= 10;
                } else {
                    spd = 0;
                }

                // Angle
                float angle = aNMEA[8].length() > 0 ? Float.parseFloat(aNMEA[8]) : -1;

                if (lat != -255 && lon != -255 && aNMEA[9].length() > 0) {
                    synchronized (pos) {
                        pos.lat = lat;
                        pos.lon = lon;
                        pos.speed = spd;
                        pos.angle = angle;
                        pos.date = aNMEA[9] + aNMEA[1].substring(0, 6);
                        pos.btime = timeToBtime(aNMEA[1]);
                        pos.status = GpsPosition.STATUS_OK;
                    }
                } else {
                    pos.status = GpsPosition.STATUS_NO_LOC;
                }

            } catch (NumberFormatException ex) {
                pos.status = GpsPosition.STATUS_NO_LOC;
                Logger.log(this + ".parseNmea(" + nmea + "):135", ex);
            } finally {
                return true;
            }
        } else if (aNMEA[0].compareTo("GPGGA") == 0) {
            try {
                pos.nbSatellites = aNMEA[7].length() > 0 ? Integer.parseInt(aNMEA[7]) : -1;
                if (aNMEA[9].length() > 0) {
                    double alt = (double) (long) Double.parseDouble(aNMEA[9]) * 10;
                    alt /= 10;
                    if (alt != 0) {
                        pos.altitude = alt;
                    }
                }
            } catch (NumberFormatException ex) {
                Logger.log(this + ".parseNmea(" + nmea + "):209", ex);
            }
        } else if (aNMEA[0].compareTo("GPGSA") == 0) {
            try {
                if (aNMEA[15].length() > 0) {
                    pos.dop = Double.parseDouble(aNMEA[15]);
                }
            } catch (NumberFormatException ex) {
                Logger.log(this + ".parseNmea(" + nmea + "):154", ex);
            }
        } else if (aNMEA[0].compareTo("GPGSV") == 0) {
            try {
                // If it's the first sentence, we have to set the number of satellites to 0
                if (Integer.parseInt(aNMEA[2]) == 1) {
                    tempNbStats = 0;
                }

            } catch (Exception ex) {
                Logger.log(this + ".parseNmea(" + nmea + "):207", ex, true);
            }

            int sat;
            for (int i = 7; i < aNMEA.length; i += 4) {
                try {
                    sat = aNMEA[i].length() == 0 ? 0 : Integer.parseInt(aNMEA[i]);
                    if (sat > 0) {
                        tempNbStats++;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    Logger.log(this + ".parseNmea(" + nmea + "):218", ex, true);
                    break;
                } catch (NumberFormatException ex) {
                    Logger.log(this + ".parseNmea(" + nmea + "):221", ex, true);
                } catch (Exception ex) {
                    Logger.log(this + ".parseNmea(" + nmea + "):224", ex, true);
                }
            }

            try {
                // If this is the last sentence, we save the result
                if (aNMEA[1].compareTo(aNMEA[2]) == 0) {
                    pos.nbSatellites = tempNbStats;
                }

            } catch (Exception ex) {
                Logger.log(this + ".parseNmea(" + nmea + "):235", ex, true);
            }
        } else if (aNMEA[0].equals("PSRFTXT")) {
            // This is the complementary data
            int p;
            if ((p = aNMEA[1].indexOf(':')) != -1) {
                listener.positionAdditionnalReceived(aNMEA[1].substring(0, p).toLowerCase(), aNMEA[1].substring(p + 1).trim());
            }
        }

        return false;
    }

    /**
     * Open the serial port.
     *
     * @return If the port could be opened
     * @see #portClose
     */
    private boolean portOpen() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".portOpen();", true);
        }

        portClose();

        try {
            if (portSpeed >= 0) {
                comm = (CommConnection) Connector.open("comm:com" + portNumber + ";baudrate=" + portSpeed + ";blocking=on;autocts=off;autorts=off");
                commIs = comm.openInputStream();
                commOs = comm.openOutputStream();
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".portOpen:1", ex, true);
            }
        }


        // We skip data already in buffer
        if (commIs != null) {
            try {
                commIs.skip(commIs.available());
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".portOpen:2", ex, true);
                }
            }
        }

        return true;
    }

    /**
     * Close the serial port.
     *
     * @return If everything went well
     */
    private boolean portClose() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".portClose();");
        }

        pos.status = GpsPosition.STATUS_NO_SIGNAL;
        pos.nbSatellites = -2;

        if (commOs != null) {
            try {
                commOs.close();
            } catch (Exception ex) {
                Logger.log(this + ".portClose:1", ex, true);
            }

            commOs = null;
        }

        if (commIs != null) {
            try {
                commIs.close();
            } catch (Exception ex) {
                Logger.log(this + ".portClose:2", ex, true);
            }

            commIs = null;
        }

        if (comm != null) {
            try {
                comm.close();
            } catch (Exception ex) {
                Logger.log(this + ".portClose:3", ex, true);
            }

            comm = null;
        }

        return true;
    }

    /**
     * Read a line (without "\r", "\n", "$") from the GPS comm connection.
     *
     * @return Line
     */
    private String readLine() {
        char ch;
        try {
            // We don't need this as we are in blocking mode
            ch = (char) commIs.read();

            if (ch != '\r' && ch != '\n' && ch != '$') {
                buff.append(ch);
            } else {
                if (buff.length() > 0) {
                    String temp = buff.toString();
                    buff.setLength(0);
                    if (NB_FRAMES_TO_SHOW != 0 && nbOkSentences < NB_FRAMES_TO_SHOW) {
                        Logger.log("NMEA[" + nbOkSentences + "] : " + temp);
                    }
                    return temp;
                }
            }
        } catch (Exception ex) {
            if (commIs != null) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".readLine / buff=\"" + buff.toString() + "\"", ex, true);
                }
            }
        }
        return null;
    }

    /**
     * Treat NMEA data.
     *
     * It reads a line, check the checksum, parse the NMEA data and return TRUE
     * if the parseNMEA method returned TRUE (which means a position was found).
     *
     * @return TRUE a GPS position was found
     */
    public boolean treat() {
        String line = "";
        try {
            for (int i = 0; i < 10 && line != null; i++) {
                line = readLine();
                if (line != null && checksum(line)) {
                    return parseNMEA(line);
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".treat", ex, true);
            }
        }

        return false;
    }

    /**
     * Do a checksum of an NMEA sentence.
     *
     * @param sent NMEA sentence to test
     * @return If the checksum is valid
     */
    private boolean checksum(String sent) {
        int iCheckSum = 0;

        if (sent.length() < 10) {
            return false;
        }

        String sToCheckSum, sGPSCheckSum;
        int iGPSCheckSum;

        try {
            sToCheckSum = sent.substring(0, (sent.length() - 3));
            sGPSCheckSum =
                    sent.substring((sent.length() - 2), sent.length());
            iGPSCheckSum =
                    Integer.parseInt(sGPSCheckSum, 16);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".treat", ex);
            }
            return false;
        }

        // XOR checksum
        int l = sToCheckSum.length();
        for (int i = 0; i < l; i++) {
            iCheckSum ^= sToCheckSum.charAt(i);
        }

        // We reduce it to a 2 letters Hexa numbers
        iCheckSum %= 256;

        if (iCheckSum == iGPSCheckSum) { // Checksum is OK
            ++nbOkSentences;
            if (tempNbStats == -2) {
                tempNbStats = -1;
            }
            return true;
        } else {
            if (Logger.BUILD_WARNING) {
                Logger.log(this + ".NMEAChecksum : Wrong checksum : " + iCheckSum + " / " + iGPSCheckSum + "\n");
            }
            return false;
        }
    }

    /**
     * Start the NMEA parser.
     */
    public synchronized void start() {
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    /**
     * Stop the NMEA parser.
     */
    public void stop() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".portClose();");
        }
        portClose();

        try {
            thread.join();
        } catch (InterruptedException ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".stop", ex, true);
            }
        }
    }

    /**
     * Main thread
     */
    public void run() {
//        if (Logger.BUILD_DEBUG) {
//            Logger.log(this + ".run();", true);
//        }
        portOpen();
        while (commIs != null) {
            if (treat()) {
//                if (Logger.BUILD_DEBUG) {
//                    Logger.log(this + ".run: Position received !", true);
//                }
                listener.positionReceived(pos);
            }
        }
    }
    private String str;

    /**
     * String representation of the GpsNmeaParser.
     *
     * @return The representation of the GpsNmeaParser with port number and
     * speed
     */
    public String toString() {
        if (str == null) {
            str = "GpsNmeaParser{" + portNumber + ":" + portSpeed + "}";
        }
        return str;
    }
}
