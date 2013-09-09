package org.javacint.gps;

/**
 * Handle the GPS chip state change.
 */
public interface GpsStatesHandler {
        /**
         * On startup.
         * This method is called before startup.
         */
	void prepareGpsStart();
	
        /**
         * On shutdown.
         * This method is called before shutdown.
         */
	void prepareGpsStop();
}
