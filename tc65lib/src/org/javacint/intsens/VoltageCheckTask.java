package org.javacint.intsens;

import java.util.TimerTask;
import org.javacint.at.ATExecution;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

/**
 * Voltage checking timer task base class.
 */
public abstract class VoltageCheckTask extends TimerTask {

    private final int step;
    private int lastVoltage;

    public VoltageCheckTask(int step) {
        this.step = step;
    }

    public void run() {
        try {
            int voltage = ATExecution.getVoltage();
            if (Math.abs(voltage - lastVoltage) > step) {
                lastVoltage = voltage;
                changed(voltage);
            }
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("VoltageCheckTask.run", ex, true);
            }

            // If we fail, it's best that we don't run anymore
            cancel();
        }
    }

    public void schedule() {
        // Every two minutes
        Timers.getSlow().schedule(this, 0, 2 * 60 * 1000);
    }

    public abstract void changed(int voltage);
}
