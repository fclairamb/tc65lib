package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Timer;
import java.util.TimerTask;
import org.javacint.logging.Logger;
import org.javacint.task.Timers;

/**
 * AT URC Queue handler.
 * This class queues URC events and send them to an other ATCommandListener.
 */
public class ATURCQueueHandler implements ATCommandListener {

    private final ATCommandListener listener;
    private final Timer timer;
    private static final boolean LOG = true;

    private class URCTask extends TimerTask {

        private final String urc;

        public URCTask(String urc) {
            this.urc = urc;
        }

        public void run() {
            if (LOG && Logger.BUILD_DEBUG) {
                Logger.log("URCTask[" + urc + "].run()...");
            }
            listener.ATEvent(urc);
            if (LOG && Logger.BUILD_DEBUG) {
                Logger.log("URCTask[" + urc + "].run();");
            }
        }
    }

    public ATURCQueueHandler(ATCommandListener listener) {
        this(listener, Timers.getSlow());
    }

    public ATURCQueueHandler(ATCommandListener listener, Timer timer) {
        this.listener = listener;
        this.timer = timer;
    }

    public void ATEvent(String urc) {
        if (LOG && Logger.BUILD_DEBUG) {
            Logger.log("ATURCQueueHandler.ATEvent(\"" + urc + "\");");
        }
        timer.schedule(new URCTask(urc), 0);
    }

    public void RINGChanged(boolean bln) {
    }

    public void DCDChanged(boolean bln) {
    }

    public void DSRChanged(boolean bln) {
    }

    public void CONNChanged(boolean bln) {
    }
}
