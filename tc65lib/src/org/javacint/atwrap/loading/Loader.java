package org.javacint.atwrap.loading;

import java.util.TimerTask;
import java.util.Vector;
import org.javacint.logging.Logger;
import org.javacint.watchdog.WatchdogStatusProvider;

/**
 * Program parts loading class.
 *
 * @author Florent Clairambault 
 */
public class Loader extends TimerTask implements WatchdogStatusProvider {

	private Vector runnables = new Vector();
	private final long startup = System.currentTimeMillis();

	public void addRunnable(NamedRunnable runnable) {
		runnables.addElement(runnable);
	}

	public void run() {
		int total = runnables.size();

		long completeBefore = System.currentTimeMillis();
		for (int i = 0; i < total; i++) {
			try {
				NamedRunnable nr = (NamedRunnable) runnables.elementAt(i);
				if (Logger.BUILD_NOTICE) {
					Logger.log("[Loading " + (i + 1) + "/" + total + "] " + nr.getName() + "...");
				}
				long before = System.currentTimeMillis();
				nr.run();
				long took = System.currentTimeMillis() - before;
				if (Logger.BUILD_NOTICE) {
					Logger.log("[Loading " + (i + 1) + "/" + total + "] " + nr.getName() + " / " + took + "ms");
				}
			} catch (Exception ex) {
				if (Logger.BUILD_CRITICAL) {
					Logger.log("[Loading " + (i + 1) + "/" + total + "]", ex, true);
				}
			}
		}
		long completeTime = System.currentTimeMillis() - completeBefore;
		if (Logger.BUILD_NOTICE) {
			Logger.log("Loaded everything in " + completeTime + "ms.");
		}
	}

	public String getWorkingStatus() {
		return (((System.currentTimeMillis() - startup) / 1000) > 600) ? "We've been loading for more than 10 minutes!" : null;
	}
}
