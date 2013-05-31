package org.javacint.watchdog;

import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;
import org.javacint.logging.Logger;

/**
 * Watchdog manager.
 *
 * Frequently checks that all the status providers are ok and kick the actors
 * (the actual watchdog).
 */
public final class WatchdogManager extends TimerTask {

    private static volatile WatchdogManager instance = null;

    public static WatchdogManager getInstance() {
        if (instance == null) {
            instance = new WatchdogManager();
        }
        return instance;
    }
    /**
     * The watchdog actors
     */
    private final Vector watchdogActors = new Vector();
    /**
     * The watchdog status providers
     */
    private final Vector statusProviders = new Vector();
    public static final boolean LOG = false;

    /**
     * Add a status provider.
     *
     * @param provider Status provider
     */
    public void addStatusProvider(WatchdogStatusProvider provider) {
        statusProviders.addElement(provider);
    }

    /**
     * Add a watchdog actor.
     *
     * @param actor Watchdog actor
     */
    public void addWatchdogActors(WatchdogActor actor) {
        watchdogActors.addElement(actor);
    }

    /**
     * Remove a status provider.
     *
     * @param provider Status provider
     */
    public void removeStatusProvider(WatchdogStatusProvider provider) {
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
    private boolean check() {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".check();");
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
    public void kick() {
        if (Logger.BUILD_VERBOSE && LOG) {
            Logger.log(this + ".kick();");
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
     * TimerTask's run method.
     *
     * Watchdog doesn't have its own thread because it would be an overkill, it
     * just needs a (not so) frequent call.
     */
    public void run() {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".run();");
        }
        if (check()) { // If check is ok
            kick(); // We kick the watchdogs
        } else if (Logger.BUILD_NOTICE) { // If not, we don't do anything
            Logger.log("Watchdog check failed (this is not good) !", true);
        }
    }

    public String toString() {
        return "WatchdogManager";
    }
}
