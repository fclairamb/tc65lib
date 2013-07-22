package org.javacint.time;

import java.util.TimerTask;
import org.javacint.logging.Logger;

/**
 * Time retriever. It organizes the frequent retrieval of the time. It should be
 * scheduled with a timer having the time between failures.
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

    public void run() {
        long jvmTime = System.currentTimeMillis();
        try {
            if (nextTime < jvmTime) {
                long time = client.getTime();
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
