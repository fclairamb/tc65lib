package org.javacint.watchdog;

/**
 * Watchdog actor interface.
 *
 * Actual watchdog implementation should be done through this interface.
 */
public interface WatchdogActor {

    /**
     * Kick the watchdog.
     *
     * @return true if not an error was encountered
     */
    boolean kick();
}
