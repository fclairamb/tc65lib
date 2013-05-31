package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif

/**
 * Pooled ATCommand instance wrapper.
 * 
 * @author florent
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
        return send(cmd, false);
    }

    public String sendRaw(String cmd) {
        return send(cmd, true);
    }
    
    private String send(String cmd, boolean isRaw) {
        if (Thread.currentThread() == blockingThread) {
            return isRaw ? ATCommands.sendRaw(atc, cmd) : ATCommands.send(atc, cmd);
        } else {
            return isRaw ? ATCommands.sendRaw(cmd) : ATCommands.send(cmd);
        }
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
