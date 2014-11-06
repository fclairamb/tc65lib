package org.javacint.gps;

import java.util.Enumeration;
import java.util.Vector;

public class GPSPositionDispatcher implements GpsPositionListener {

    private final Vector listeners = new Vector();

    public GPSPositionDispatcher() {
    }

    public void addListener(GpsPositionListener listener) {
        listeners.addElement(listener);
    }

    public void positionReceived(GpsPosition pos) {
        for (Enumeration en = listeners.elements(); en.hasMoreElements();) {
            GpsPositionListener listener = (GpsPositionListener) en.nextElement();
            listener.positionReceived(pos);
        }
    }

    public void positionAdditionnalReceived(String type, String value) {
        for (Enumeration en = listeners.elements(); en.hasMoreElements();) {
            GpsPositionListener listener = (GpsPositionListener) en.nextElement();
            listener.positionAdditionnalReceived(type, value);
        }
    }
}
