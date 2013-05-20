package org.javacint.watchdog;

import com.siemens.icm.io.ATCommandFailedException;
import com.siemens.icm.io.OutPort;
import java.io.IOException;
import java.util.Vector;
import org.javacint.logging.Logger;

/**
 * Watchdog on GPIO based hardware management class.
 *
 * This implementation uses Java API based GPIO control.
 */
public class WatchdogOnJavaGpio implements WatchdogActor {

	/**
	 * Gpio used for the hardware watchdog
	 */
	private final int gpio;
	private final boolean inverted;
	private OutPort port;

	/**
	 * Constructor for an instance without settings management
	 *
	 * @param gpio GPIO number to use (1 to 10)
	 * @param inverted If the GPIO is inverted, false means the GPIO spends most
	 * of its time in low state
	 */
	public WatchdogOnJavaGpio(int gpio, boolean inverted) {
		//_atc = atc;

		this.gpio = gpio;
		this.inverted = inverted;
		init();
	}

	/**
	 * Set the state of the GPIO
	 *
	 * @param state on or off
	 * @return The result of the AT command execution
	 * @throws com.siemens.icm.io.ATCommandFailedException
	 */
	private void setState(boolean state) throws ATCommandFailedException, IOException {
		if (port != null) {
			port.setValue(state ? 1 : 0);
		}
	}

	/**
	 * Reverse the state of the GPIO
	 *
	 * @return TRUE if there wasn't any error
	 */
	public boolean kick() {
		try {

			//_state = !_state;
			setState(inverted ? false : true);
			Thread.sleep(1000);
			setState(inverted ? true : false);

		} catch (Exception ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log("WatchdogOnGpio.kick", ex);
			}
			return false;
		}
		return true;
	}

	/**
	 * Prepares watchdog
	 */
	private void init() {
		try {
			port = new OutPort(new Vector() {
				{
					addElement("GPIO" + gpio);
				}
			}, new Vector() {
				{
					addElement(new Integer(0));
				}
			});
		} catch (Exception ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log("WatchdogOnGpio.init", ex);
			}
		}
	}

	public String toString() {
		return "WatchdogOnJavaGpio";
	}
}
