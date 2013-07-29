package org.javacint.watchdog;

import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

/**
 * Watchdog manager.
 *
 * Frequently checks that all the status providers are ok and kick the actors
 * (the actual watchdog).
 */
public final class WatchdogManager {

    /**
     * The watchdog actors
     */
    private static final Vector watchdogActors = new Vector();
    /**
     * The watchdog status providers
     */
    private static final Vector statusProviders = new Vector();
    public static final boolean LOG = false;

    /**
     * Add a status provider.
     *
     * @param provider Status provider
     */
    public static void add(WatchdogStatusProvider provider) {
        statusProviders.addElement(provider);
    }

    /**
     * Add a watchdog actor.
     *
     * @param actor Watchdog actor
     */
    public static void add(WatchdogActor actor) {
        watchdogActors.addElement(actor);
    }

    /**
     * Start the watchdog manager.
     *
     * @param offset Offset time.
     * @param period Period.
     */
    public static void start(long offset, long period) {
        Timers.getFast().schedule(new TimerTask() {
            public void run() {
                staticRun();
            }
        }, offset, period);
    }

    /**
     * Remove a status provider.
     *
     * @param provider Status provider
     */
    public static void remove(WatchdogStatusProvider provider) {
        synchronized (statusProviders) {
            if (statusProviders.contains(provider)) {
                statusProviders.removeElement(provider);
            }
        }
    }

    /**
     * Check our functional status.
     *
     * @return TRUE if everything is OK
     */
    private static boolean check() {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(ME + ".check();");
        }

        try {
            synchronized (statusProviders) {
                for (Enumeration en = statusProviders.elements(); en.
                        hasMoreElements();) {
                    String error = ((WatchdogStatusProvider) en.nextElement()).
                            getWorkingStatus();
                    if (error != null) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log("Watchdog.check: " + error, true);
                        }
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Watchdog.check", ex);
            }
            return false;
        }
    }

    /**
     * Send all the watchdog actors the "keep-alive" signal
     */
    public static void kick() {
        if (Logger.BUILD_VERBOSE && LOG) {
            Logger.log(ME + ".kick();");
        }
        try {
            synchronized (watchdogActors) {
                for (Enumeration en = watchdogActors.elements(); en.
                        hasMoreElements();) {
                    ((WatchdogActor) en.nextElement()).kick();
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Watchdog.signal", ex);
            }
        }
    }

    /**
     * Stop the watchdogs. This stops the watchdog to let them
     */
    public static void stop() {
    }

    /**
     * TimerTask's run method.
     *
     * Watchdog doesn't have its own thread because it would be an overkill, it
     * just needs a (not so) frequent call.
     */
    private static void staticRun() {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(ME + ".run();");
        }
        if (check()) { // If check is ok
            kick(); // We kick the watchdogs
        } else if (Logger.BUILD_NOTICE) { // If not, we don't do anything
            Logger.log("Watchdog check failed (this is not good) !", true);
        }
    }
    private static final String ME = "WatchdogManager";
}
