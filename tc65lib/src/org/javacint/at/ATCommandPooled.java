package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif

import org.javacint.logging.Logger;

/**
 * Pooled ATCommand instance wrapper.
 */
public class ATCommandPooled {

    private final ATCommand atc;
    private Thread blockingThread = null;

    ATCommandPooled(ATCommand atc) {
        this.atc = atc;
    }

    ATCommand getATCommand() {
        return atc;
    }

    Thread getBlockingThread() {
        return blockingThread;
    }

    void setBlockingThread() {
        blockingThread = Thread.currentThread();
    }

    public String send(String cmd) {
        return sendRaw(cmd + '\r');
    }

    public String sendRaw(String cmd) {
        if (Thread.currentThread() != blockingThread) {
            Logger.log("You're using an ATC that belongs to " + blockingThread.getName() + ".");
        }
        return ATCommands.sendRaw(atc, cmd);
    }

    public String sendLongRaw(String cmd) {
        if (Thread.currentThread() != blockingThread) {
            Logger.log("You're using an ATC that belongs to " + blockingThread.getName() + ".");
        }
        return ATCommands.sendLongRaw(atc, cmd);
    }

    public void release() {
        if (Thread.currentThread() == blockingThread) {
            synchronized (ATCommands.class) {
                blockingThread = null;
                ATCommands.class.notify();
            }
        }
    }
}
