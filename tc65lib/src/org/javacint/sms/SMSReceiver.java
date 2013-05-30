/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.sms;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Enumeration;
import java.util.Vector;
import org.javacint.at.ATCommands;
import org.javacint.logging.Logger;

/**
 *
 * @author florent
 */
// TODO: The actual SMS receiving code. I can't copy/paste my current code
// for this part because it sucks a lot.
public class SMSReceiver implements ATCommandListener {

    private final Vector consumers = new Vector();

    private SMSReceiver() {
    }

    public void addConsumer(SMSConsumer c) {
        synchronized (consumers) {
            consumers.addElement(c);
        }
    }

    public void removeConsumer(SMSConsumer c) {
        synchronized (consumers) {
            consumers.removeElement(c);
        }
    }

    private boolean handleSMS(String from, String content) {
        synchronized (consumers) {
            for (Enumeration e = consumers.elements(); e.hasMoreElements();) {
                SMSConsumer cons = (SMSConsumer) e.nextElement();
                try {
                    if (cons.smsReceived(from, content)) {
                        return true;
                    }
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".handleSMS( " + from + ", " + content + " ) failed in " + cons, ex, true);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Start SMS reception.
     *
     * This methold should only be called once all SMS receivers have been
     * called.
     */
    public void start() {
        ATCommands.addListener(this);
        ATCommands.sendUrc("at+cmgf=1");
        ATCommands.sendUrc("at+cnmi=1,1");
    }

    public void stop() {
        ATCommands.removeListener(this);
    }

    public void ATEvent(String ate) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".ATEvent( \"" + ate + "\" )");
        }
    }

    public void RINGChanged(boolean bln) {
    }

    public void DCDChanged(boolean bln) {
    }

    public void DSRChanged(boolean bln) {
    }

    public void CONNChanged(boolean bln) {
    }

    public String toString() {
        return "SMSR";
    }
}
