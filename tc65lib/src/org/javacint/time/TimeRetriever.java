package org.javacint.time;

import java.util.TimerTask;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

/**
 * Time retriever. It organizes the frequent retrieval of the time. It should be
 * scheduled with a slow timer.
 */
public class TimeRetriever extends TimerTask {

    private final TimeClient client;
    private final long timeBetweenSuccesses, timeBetweenFailures;
    private long nextTime;

    public TimeRetriever(TimeClient client, long timeBetweenSuccesses, long timeBetweenFailures) {
        this.client = client;
        this.timeBetweenSuccesses = timeBetweenSuccesses;
        this.timeBetweenFailures = timeBetweenFailures;
    }

    public TimeRetriever(TimeClient client) {
        this(client, 24 * 3600 * 1000, 1800 * 1000);
    }

    public void schedule() {
        long period = timeBetweenFailures / 3;
        if (period < 30000) {
            period = 30000;
        }
        if (timeBetweenSuccesses == 0) { // once
            Timers.getSlow().schedule(this, 0);
        } else {
            Timers.getSlow().schedule(this, 0, period);
        }
    }

    public void run() {
        long jvmTime = System.currentTimeMillis();
        try {
            if (nextTime < jvmTime) {
                long time = client.getTime();
                if (time == 0) {
                    throw new IllegalArgumentException("Time cannot be zero !");
                }
                DateManagement.setCurrentTime(time);
                nextTime = jvmTime + timeBetweenSuccesses;
            }
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Client " + client + " had an issue ", ex, true);
            }
            nextTime = jvmTime + timeBetweenFailures;
        }
    }
}
