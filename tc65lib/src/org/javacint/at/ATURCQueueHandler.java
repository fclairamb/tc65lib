/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Timer;
import java.util.TimerTask;
import org.javacint.task.Timers;

/**
 *
 * @author florent
 */
public class ATURCQueueHandler implements ATCommandListener {

    private final ATCommandListener listener;
    private final Timer timer;

    private class URCTask extends TimerTask {

        private final String urc;

        public URCTask(String urc) {
            this.urc = urc;
        }

        public void run() {
            listener.ATEvent(urc);
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
