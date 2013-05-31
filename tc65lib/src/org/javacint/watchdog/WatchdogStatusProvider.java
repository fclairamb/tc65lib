package org.javacint.watchdog;

/**
 * Provide a working status to the watchdog manager.
 */
public interface WatchdogStatusProvider {

    /**
     * Check the status of the instance.
     *
     * @return null if everything is ok, an error message if not
     */
    String getWorkingStatus();
}
