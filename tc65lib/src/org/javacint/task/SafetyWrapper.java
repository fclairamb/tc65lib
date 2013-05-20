/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.task;

import java.util.TimerTask;
import org.javacint.logging.Logger;

/**
 *
 * @author florent
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
