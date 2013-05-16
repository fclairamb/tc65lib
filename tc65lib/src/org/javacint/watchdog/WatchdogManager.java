package org.javacint.watchdog;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;
import org.javacint.logging.Logger;

/**
 * Watchdog manager: Handles watchdogs and watchdog status providers
 */
public final class WatchdogManager extends TimerTask {

	/**
	 * The watchdog actors
	 */
	private final Vector watchdogActors = new Vector();
	/**
	 * The watchdog status providers
	 */
	private final Vector statusProviders = new Vector();
	public static boolean LOG = false;

	/**
	 * Default constructor
	 */
	public WatchdogManager() {
		//autoLoadEmbeddedWatchdogIfPossible();
	}

	/**
	 * Add a status provider
	 *
	 * @param provider Status provider
	 */
	public void addStatusProvider(WatchdogStatusProvider provider) {

		if (provider == null) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log("Added a null watchdog status provider...");
			}
			return;
		}
		synchronized (statusProviders) {
			statusProviders.addElement(provider);
		}
	}

	public void addWatchdogActors(WatchdogActor actor) {
		if (actor == null) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log("Added a null watchdog actor...");
			}
			return;
		}
		synchronized (watchdogActors) {
			watchdogActors.addElement(actor);
		}
	}

	/**
	 * Remove a status provider
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

	private boolean check() {
		// We just check if ... 

		if (Logger.BUILD_DEBUG && LOG) {
			Logger.log(this + ".check();");
		}

		boolean result = true;

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
						result = false;
					}
				}
			}

			return result;
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
	 * This method should be called regularly (every 5 to 15s) by a background
	 * thread.
	 *
	 * Watchdog doesn't have its own thread because it would be an overkill, it
	 * just needs a (not so) frequent call.
	 */
	public void work() {
		if (check()) {
			kick();
		} else if (Logger.BUILD_NOTICE) {
			Logger.log("Watchdog check failed (this is not good) !", true);
		}
	}

	public void getDefaultSettings(Hashtable settings) {
	}

	public void settingsChanged(String[] settings) {
		if (Logger.BUILD_DEBUG) {
			Logger.log("WatchdogManager.settingsChanged( settings[" + settings.length + "] );");
		}
	}

	public void run() {
		if (Logger.BUILD_DEBUG && LOG) {
			Logger.log(this + ".run();");
		}
		work();
	}

	public String toString() {
		return "WatchdogManager";
	}
}
