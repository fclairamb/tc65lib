package org.javacint.gps;

import java.util.Hashtable;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * High level GPS chip management. This is mostly useful for GPS chip
 * auto-detection.
 */
public class GpsManager implements Runnable, SettingsProvider {

    // CONSTANTS
    private final int PORT_SPEEDS[] = {4800, 9600, 19200, 38400, 115200};
    private final String SETTING_GPS_ENABLED = "gps.enabled";
    private final String SETTING_GPS_PORT_NB = "gps.portnb";
    private final String SETTING_GPS_PORT_SPEED = "gps.portspeed";
    // MEMBERS
    private final GpsPositionListener listener;
    private GpsNmeaParser parser;
    private boolean keepWorking = true;
    private boolean enabled;
    private Thread thread;
    // STATIC
    private static final Object serialPortsDetectionLock_ = new Object();
    private GpsStatesHandler statesHandler;

    /**
     * GPS Manager constructor
     *
     * @param atc ATCommand
     */
    public GpsManager(GpsPositionListener listener) {
        this.listener = listener;
//        if (Logger.BUILD_DEBUG) {
//            Logger.log("GpsManager.init()");
//        }
        Settings.addProvider(this);
    }

    public void setGpsStatesHandler(GpsStatesHandler h) {
        statesHandler = h;
    }

    private void loadSettings() {
        parseSetting(SETTING_GPS_ENABLED);
    }

    private void openGps(int portId, int portSpeed) {
        if (Logger.BUILD_NOTICE) {
            Logger.log(this + ".openGps( " + portId + ", " + portSpeed + " );", true);
        }

        parser = new GpsNmeaParser(listener, portId, portSpeed);
        parser.start();

        if (statesHandler != null) {
            statesHandler.gpsStart();
        }
    }

    private void closeGps() {
        if (Logger.BUILD_NOTICE) {
            Logger.log(this + ".closeGps();");
        }
        if (parser != null) {
            parser.stop();
            parser = null;
        }

        if (statesHandler != null) {
            statesHandler.gpsStop();
        }
    }

    /**
     * The thread method.
     *
     * This method loads the the GPS NMEA parser and if the parser doesn't parse
     * anything it launches the autodetection.
     */
    public void run() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".run();");
        }
        try {
            if (!Settings.getBool(SETTING_GPS_ENABLED)) {
                return;
            }

            if (parser != null) {
                closeGps();
            }

            int portId = 0;
            int portSpeed = 0;
            try {
                portId = Settings.getInt(SETTING_GPS_PORT_NB);
                portSpeed = Settings.getInt(SETTING_GPS_PORT_SPEED);
            } catch (Exception ex) {
                if (Logger.BUILD_DEBUG) {
                    Logger.log(this + ".run:114", ex, true);
                }
            }
            synchronized (serialPortsDetectionLock_) {
                if (portSpeed != 0) {
                    // We start the GPS on this port
                    openGps(portId, portSpeed);
                    sleep(5000);
                    if (parser.getNbSentences() > 0) {
                        if (Logger.BUILD_NOTICE) {
                            Logger.log("GPS chip is working fine !", true);
                        }
                        // Thread ends now
                        return;
                    } else {
                        if (Logger.BUILD_NOTICE) {
                            Logger.log("No data was received from the GPS chip...", true);
                        }
                        closeGps();
                    }
                }

                // We could reduce the detection time by detecting the two ports at the same time.
                // and maybe also by reducing the time to wait before checking for sentences

                for (int j = 1; j >= 0; --j) { // Port: ASC0, ASC1
                    for (int i = 0; i < PORT_SPEEDS.length && keepWorking; ++i) { // Speed: 2400, 4800, 9600, 19200, 38400, 57600, 115200
                        portId = j;
                        portSpeed = PORT_SPEEDS[i];

                        openGps(portId, portSpeed);
                        sleep(5000);
                        if (parser.getNbSentences() > 0) {
                            if (Logger.BUILD_NOTICE) {
                                Logger.log("GPS chip was found on ASC" + portId + ":" + portSpeed, true);
                            }
                            Settings.set(SETTING_GPS_PORT_NB, portId + "");
                            Settings.set(SETTING_GPS_PORT_SPEED, portSpeed + "");
                            // This is rare enough that we can afford to save the file
                            Settings.save();
                            return;
                        } else {
                            closeGps();
                        }
                    } // for j
                } // for i
            } // synchronized ( Global.portDetectionLock )
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".run", ex, true);
            }
        } finally {
            thread = null;
        }
    }

    /**
     * Sleeping
     *
     * @param time Sleeping
     */
    private void sleep(long time) {
        try {
            synchronized (this) {
                this.wait(time);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".sleep", ex, true);
            }
        }
    }

    /**
     * Start the GPSManager
     */
    private void startThread() {
        try {
            if (Logger.BUILD_DEBUG) {
                Logger.log(this + ".start();");
            }

            keepWorking = true;
            synchronized (this) {

                if (thread != null) {
                    if (Logger.BUILD_WARNING) {
                        Logger.log("GPS Thread is already started!");
                    }
                    return;
                }

                thread = new Thread(this, "gpm");
                thread.start();
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("gps.start", ex, true);
            }
        }
    }

    /**
     * Stop the GPSManager
     */
    public void stop() {
        if (Logger.BUILD_DEBUG) {
            Logger.log("gps.stop();");
        }

        keepWorking = false;
        closeGps();

        // To interrupt the sleep
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void getDefaultSettings(Hashtable settings) {
        if (Logger.BUILD_DEBUG) {
            Logger.log("GpsManager.getDefaultSettings();");
        }
        settings.put(SETTING_GPS_ENABLED, "1");
        settings.put(SETTING_GPS_PORT_NB, "0");
        settings.put(SETTING_GPS_PORT_SPEED, "4800");
    }

    public void settingsChanged(String[] settings) {
        for (int i = 0; i < settings.length; ++i) {
            parseSetting(settings[i]);
        }
    }

    private boolean parseSetting(String name) {
        if (name.compareTo(SETTING_GPS_ENABLED) == 0) {
            setEnabled(Settings.getBool(SETTING_GPS_ENABLED));
        } else {
            return false;
        }

        return true;
    }

    private void setEnabled(boolean enabled) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".setEnabled( " + enabled + " );");
        }
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (Logger.BUILD_DEBUG) {
                Logger.log(this + ".setEnabled( " + enabled + " ): confirmed!");
            }
            if (this.enabled) {
                startThread();
            } else {
                stop();
            }
        }
    }

    public void start() {
        loadSettings();
    }

    public String toString() {
        return "GPSManager";
    }
}
