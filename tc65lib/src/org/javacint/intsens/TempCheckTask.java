package org.javacint.intsens;

import java.util.TimerTask;
import org.javacint.at.ATExecution;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

/**
 * Temperature checking timer task base class.
 */
public abstract class TempCheckTask extends TimerTask {

    private final int diffSend;
    private int lastTemp = -1000;

    public TempCheckTask() {
        this(2);
    }

    public TempCheckTask(int diffSend) {
        this.diffSend = diffSend;
    }

    public void run() {
        try {
            if (lastTemp == -1000) {
                ATExecution.enableTemp(true);
                lastTemp = -500;
            }

            int temp = ATExecution.getTemp();

            if (Math.abs(temp - lastTemp) >= diffSend) {
                lastTemp = temp;
                changed(temp);
            }

            if (temp < -100) {
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

    public String toString() {
        return "TempCheckTask";
    }

    public void schedule() {
        // Every two minutes
        Timers.getSlow().schedule(this, 0, 2 * 60 * 1000);
    }

    public abstract void changed(int temp);
}
