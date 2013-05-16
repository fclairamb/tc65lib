package org.javacint.watchdog;

/**
 * Gives the watchdog manager a working status.
 * @author Florent Clairambault 
 */
public interface WatchdogStatusProvider {
	/**
	 * Check if the status of the class is ok
	 * @return null if everything is ok, an error message if not
	 */
	String getWorkingStatus();
}
