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

    /**
     * Stop the watchdog. It might be necessary to stop the watchdog to let the
     * OTAP happen. On hardware watchdog, it should be implemented by calling the kick method.
     */
    public void stop();
}
