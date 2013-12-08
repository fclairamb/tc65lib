package org.javacint.gps;

import org.javacint.common.Bytes;
import org.javacint.common.Math2;
import org.javacint.time.DateManagement;

/**
 * GPS Position.
 */
public class GpsPosition {

    public static final byte STATUS_OK = 0;
    public static final byte STATUS_NO_LOC = -1;
    public static final byte STATUS_NO_SIGNAL = -2;
    /**
     * Date
     */
    public String date;
    /**
     * Latitude
     */
    public double lat;
    /**
     * Longitude
     */
    public double lon;
    /**
     * Speed
     */
    public double speed;
    /**
     * Altitude
     */
    public double altitude;
    /**
     * Angle
     */
    public float angle;
    /**
     * Dillution of precision
     */
    public double dop;
    /**
     * Number of satellites
     */
    public int nbSatellites = -2;
    /**
     * Bogus time
     */
    public int btime;
    /**
     * Status
     */
    public byte status;

    /**
     * Creation of an empty position
     */
    public GpsPosition() {
        lat = 0;
        lon = 0;
        speed = 0;
        altitude = 0;
        date = "";
        dop = 0;
    }

    /**
     * Copy constructor
     *
     * @param source Source GpsPosition to copy from
     */
    public GpsPosition(GpsPosition source) {
        status = source.status;
        lat = source.lat;
        lon = source.lon;
        date = source.date;
        speed = source.speed;
        angle = source.angle;
        altitude = source.altitude;
        dop = source.dop;
        btime = source.btime;
        nbSatellites = source.nbSatellites;
    }

    /**
     * Get the compact view of a GPS Position
     *
     * @return Compact view of GPS Position :
     * YYMMDDhhmmss,lat,lon,speed,altitude
     */
    public String toString() {

        return "Loc{sta:" + this.status + ",date:" + this.date + ",lat:" + this.lat + ",lon:" + this.lon + ",spd:" + this.speed + ",ang:" + this.angle + ",alt:" + this.altitude + ",sat:" + this.nbSatellites + ",dop:" + this.dop + "}";
    }

    /**
     * Convert the position to a byte array.
     *
     * These are the possible sizes:
     * <ul>
     * <li>1 - No reception, we only have the number of satellites.</li>
     * <li>5 - No reception, we have the time and the number of satellites.</li>
     * <li>12 - Reception, but not moving.</li>
     * <li>14 - Reception and moving.</li>
     * </ul>
     *
     * @return
     */
    public byte[] toBytes() {
        if (status == STATUS_OK) {
            byte[] bytes = new byte[speed != 0 ? 14 : 12];

            long time = DateManagement.stringDateToTimestamp(date) / 1000;


            Bytes.longToUInt32Bytes(time, bytes, 0);  // date [0...3]
            Bytes.floatToBytes((float) lat, bytes, 4); // lat [4...7]
            Bytes.floatToBytes((float) lon, bytes, 8); // lon [8...11]
            if (bytes.length == 14) {
                Bytes.intTo2Bytes((int) speed, bytes, 12); // speed [12...13]
            }
            return bytes;
        } else {
            // Maybe we could get the time from the GPS chip
            long time = DateManagement.stringDateToTimestamp(date) / 1000;

            // If we couldn't, maybe we have it some other way
            if (time < DateManagement.THE_PAST) {
                time = DateManagement.time();
            }

            // If we don't have it, it's best to do not report it
            if (time < DateManagement.THE_PAST) {
                byte[] bytes = new byte[1];
                bytes[0] = (byte) nbSatellites;
                return bytes;
            } else {
                byte[] bytes = new byte[5];
                Bytes.longToUInt32Bytes(time, bytes, 0);  // date [0...3]
                bytes[4] = (byte) nbSatellites;
                return bytes;
            }
        }
    }

    /**
     * Distance between two positions
     *
     * @param pos Position to calculate the distance to
     * @return Distance between two positions (in meters)
     */
    public double distanceTo(GpsPosition pos) {
        if ((pos.lat == lat && pos.lon == lon)
                || (lat == 0 && lon == 0)
                || (pos.lat == 0 && pos.lon == 0)) {
            return 0;
        }
        double latA = this.lat * Math.PI / 180,
                lonA = this.lon * Math.PI / 180,
                latB = pos.lat * Math.PI / 180,
                lonB = pos.lon * Math.PI / 180;

        double radius = 6378;

        return 1000 * radius * (Math.PI / 2 - Math2.asin(Math.sin(latB) * Math.sin(latA) + Math.cos(lonB - lonA) * Math.cos(latB) * Math.cos(latA)));
    }

    /**
     * Create a clone of this object
     *
     * @return Clone of this object
     */
    public synchronized GpsPosition clone() {
        return new GpsPosition(this);
    }

    public boolean hasLocation() {
        return status == STATUS_OK;
    }
}
