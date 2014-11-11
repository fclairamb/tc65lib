package org.javacint.gps;

import java.util.Hashtable;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * GPS Position filtering code
 */
public class GpsPositionFilter implements GpsPositionListener, SettingsProvider {

    private final GpsPositionListener listener;
    private SpeedFilter speedFilters[] = new SpeedFilter[]{};

// This is handled as settings now:
//        new SpeedFilter(60, 30),
//        new SpeedFilter(10, 5),
//        new SpeedFilter(0, 900)
    /**
     * Constructor with a gps position listener
     *
     * @param listener Listener for the positions
     */
    public GpsPositionFilter(GpsPositionListener listener) {
        this.listener = listener;

        // We read the current settings
        Settings.addProvider(this);
        parseSetting(SETTING_LOG_RCVD);
        parseSetting(SETTING_LOG_SENT);
        parseSetting(SETTING_LOG_PERIOD);
        parseSetting(SETTING_FILTER_SPEEDS);
    }

    /**
     * Get a period by considering the speed
     *
     * @param pos Position to analyze
     * @return Minimum period we should send data at
     */
    private int getSpeedPeriod(GpsPosition pos) {
        double spd = pos.speed;
        int l = speedFilters.length;

//		int period = 1800;

        // If we are stopped, we want to send it now
        if (lastSentPosition.speed > 0 && spd == 0) {
            if (Logger.BUILD_DEBUG && logPeriod) {
                Logger.log(this + ".getSpeedPeriod: We just stopped !", true);
            }
            return 0;
        } else { // If we are on the road we will try all the speed until we find one that matches
            for (int i = l - 1; i >= 0; i--) {
                if (speedFilters[i].maxSpeed <= spd) {
                    if (Logger.BUILD_DEBUG && logPeriod) {
                        Logger.log(this + ".getSpeedPeriod: Found a period of " + speedFilters[i].period + "s for speed " + spd + "km/h", true);
                    }
                    return speedFilters[i].period;
                }
            }
            if (Logger.BUILD_DEBUG && logPeriod) {
                Logger.log(this + ".getSpeedPeriod: Could not find matching period for speed " + spd + "km/h", true);
                for (int i = 0; i < l; i++) {
                    SpeedFilter filter = speedFilters[i];
                    Logger.log(this + ".getSpeedPeriod: " + filter.getPeriod() + "s for " + filter.getMinSpeed() + ".", true);
                }
            }
        }

        return 1800;
    }

    /**
     * Get a period by considering all the parameters of this position compared
     * to the previously sent positions.
     *
     * @param pos Position to analyze
     * @return The period we should send data at
     */
    private int getPeriod(GpsPosition pos) {
        return getSpeedPeriod(pos);
    }
//    private GpsPosition lastPosition = new GpsPosition();
    private GpsPosition lastSentPosition = new GpsPosition();

    /**
     * Method calling the IGpsPositionListener method
     *
     * @param pos Position to transmit to the listener
     */
    private void sendPosition(GpsPosition pos) {
        lastSentPosition = pos;
        if (logSentPosition && Logger.BUILD_NOTICE) {
            Logger.log("GPSFilter.sendPosition: " + pos, true);
        }
        listener.positionReceived(pos);
    }
    private long noGpsLastSentLoc = System.currentTimeMillis();
    private long noGpsPeriodBetweenSats = 60;

    /**
     * Listen to coming position to decide if they must be transmitted to the
     * other listener or not.
     *
     * @param pos Position to analyze
     */
    public void positionReceived(GpsPosition pos) {
        try {
            if (logReceivedPosition && Logger.BUILD_NOTICE) {
                Logger.log("GPSFilter.received: " + pos, true);
            }

            boolean send = false;

            if (pos.status == GpsPosition.STATUS_OK) {
                int period = getPeriod(pos);

                int lastSentDiff = Math.abs(pos.btime - lastSentPosition.btime);



                if (lastSentDiff >= period) {
                    send = true;
                    noGpsPeriodBetweenSats = 60;
                } else if (logPeriod && Logger.BUILD_NOTICE) {
                    Logger.log("GPSFilter.received: lastSentDiff:" + lastSentDiff + ", period:" + period + ", pos.btime:" + pos.btime + ", lastSentPosition.btime:" + lastSentPosition.btime, true);
                }
            } else {
                long timeDiff = (System.currentTimeMillis() - noGpsLastSentLoc) / 1000;
                if (logPeriod && Logger.BUILD_NOTICE) {
                    Logger.log("GPSFilter.received: no loc, sats:" + pos.nbSatellites + ", timeDiff:" + timeDiff + ", periodBetweenSats:" + noGpsPeriodBetweenSats, true);
                }
                if (timeDiff >= noGpsPeriodBetweenSats) {
                    send = true;
                    noGpsPeriodBetweenSats += 60;
                    if (noGpsPeriodBetweenSats > 60 * 60 * 6) {
                        noGpsPeriodBetweenSats = 60 * 60 * 6;
                    }
                }
            }

            if (send) {
                noGpsLastSentLoc = System.currentTimeMillis();
                sendPosition(pos.clone());
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".positionReceived", ex, true);
            }
        }
    }
    private boolean logReceivedPosition;
    private boolean logSentPosition;
    private boolean logPeriod;
    private static final String SETTING_LOG_RCVD = "gps.filter.log.rcvd";
    private static final String SETTING_LOG_SENT = "gps.filter.log.sent";
    private static final String SETTING_LOG_PERIOD = "gps.filter.log.period";
    private static final String SETTING_FILTER_SPEEDS = "gps.filter.speeds";

    /**
     * Get the default settings
     *
     * gps.log.rcvd = 0<br />
     *
     * gps.log.sent = 0<br />
     *
     * gps.filter.speeds = 3600:0,10:5,60:30<br />
     *
     * @param settings Settings hashtable to wich these values will be added
     */
    public void getDefaultSettings(Hashtable settings) {
        settings.put(SETTING_LOG_RCVD, "0");
        settings.put(SETTING_LOG_SENT, "0");
        settings.put(SETTING_LOG_PERIOD, "0");
        settings.put(SETTING_FILTER_SPEEDS, "0:3600,10:10,25:60");
    }

    public void settingsChanged(String[] settings) {
        for (int i = 0; i < settings.length; i++) {
            parseSetting(settings[i]);
        }
    }

    private void parseSetting(String settingName) {
        if (SETTING_LOG_RCVD.compareTo(settingName) == 0) {
            logReceivedPosition = Settings.getBool(settingName);
        } else if (SETTING_LOG_SENT.compareTo(settingName) == 0) {
            logSentPosition = Settings.getBool(settingName);
        } else if (SETTING_LOG_PERIOD.compareTo(settingName) == 0) {
            logPeriod = Settings.getBool(settingName);
        } else if (SETTING_FILTER_SPEEDS.compareTo(settingName) == 0) {
            parseFilterSpeeds(Settings.get(settingName));
        }
    }

    private void parseFilterSpeeds(String value) {
        try {
            String[] filters = Strings.split(',', value);
            SpeedFilter newSpeedFilters[] = new SpeedFilter[filters.length];
            for (int i = 0; i < filters.length; i++) {
                String f = filters[i];
                String[] values = Strings.split(':', f);
                newSpeedFilters[i] = new SpeedFilter(Double.parseDouble(values[0]), Integer.parseInt(values[1]));
            }
            speedFilters = newSpeedFilters;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".parseFilterSpeeds(\"" + value + "\")", ex);
            }
        }
    }

    public String toString() {
        return "GpsPositionFilter";
    }

    public void positionAdditionnalReceived(String type, String value) {
        listener.positionAdditionnalReceived(type, value);
    }

    private static class SpeedFilter {

        final double maxSpeed;
        final int period;

        public SpeedFilter(double minSpeed, int period) {
            this.maxSpeed = minSpeed;
            this.period = period;
        }

        public double getMinSpeed() {
            return this.maxSpeed;
        }

        public int getPeriod() {
            return this.period;
        }
    }
}
