package org.javacint.intsens;

import java.util.TimerTask;
import org.javacint.at.ATExecution;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

public abstract class RSSICheckTask extends TimerTask {

    private final int diffSend;
    private int last = -1000;

    public RSSICheckTask() {
        this(2);
    }

    public RSSICheckTask(int diffSend) {
        this.diffSend = diffSend;
    }

    public void run() {
        try {
            int rssi = ATExecution.getRssi();

            if (Math.abs(rssi - last) >= diffSend) {
                last = rssi;
                changed(rssi);
            }

            if (rssi < -100) {
                throw new RuntimeException("Invalid temp !");
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".run", ex, true);
            }

            // If we fail, it's best that we don't run anymore
            cancel();
        }
    }

    public void schedule() {
        schedule(60000);
    }

    public void schedule(long period) {
        // Every minute
        Timers.getFast().schedule(this, 0, period);
    }

    public abstract void changed(int level);
}
