package org.javacint.task;

import java.util.TimerTask;
import org.javacint.logging.Logger;

/**
 * Timer task safety wrapper.
 *
 * This wrapper only guarantees a runnable task won't break the Timer.
 *
 */
public class SafetyWrapper extends TimerTask {

    private final Runnable runnable;

    public SafetyWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        try {
            runnable.run();
        } catch (Throwable ex) {
            Logger.log("SafetyWrapper", ex, true);
        }
    }
}
