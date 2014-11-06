/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.gps;

import com.siemens.icm.io.OutPort;
import java.io.IOException;
import java.util.Vector;
import org.javacint.logging.Logger;

/**
 * GPS GPIO handler.
 *
 * Starts and stops the GPIO when required.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class GpsGpioHandler implements GpsStatesHandler {

    private final OutPort port;

    public GpsGpioHandler(final int gpio) throws IOException {
        port = new OutPort(new Vector() {
            {
                addElement("GPIO" + gpio);
            }
        }, new Vector() {
            {
                addElement(new Integer(0));
            }
        });
    }

    public void gpsStart() {
        try {
            port.setValue(1);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("GpsGpioHandler.prepareGpsStart", ex, true);
            }
        }
    }

    public void gpsStop() {
        try {
            port.setValue(0);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("GpsGpioHandler.prepareGpsStop", ex, true);
            }
        }
    }
}
