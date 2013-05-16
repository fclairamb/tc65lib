package org.javacint.watchdog;

import com.siemens.icm.misc.Watchdog;

/**
 * Embedded watchdog management class for the TC65 v3 / TC65i chips.
 */
public class WatchdogEmbedded implements WatchdogActor {

	/**
	 * Default constructor
	 */
	public WatchdogEmbedded() {
		this(300);
	}

	/**
	 * Constructor with specified timeout time
	 *
	 * @param time Timeout time
	 */
	public WatchdogEmbedded(int time) {
		init(time);
	}

	/**
	 * Initialization method
	 *
	 * @param timeout Watchdog timeout
	 */
	private void init(int timeout) {
		Watchdog.start(timeout);
	}

	/**
	 * Launch a signal to the watchdog
	 *
	 * @return always true
	 */
	public boolean kick() {
//		if ( Logger.BUILD_DEBUG )
//			Logger.log( "WatchdogEmbeddded.kick();" );
		Watchdog.kick();
		return true;
	}
}
