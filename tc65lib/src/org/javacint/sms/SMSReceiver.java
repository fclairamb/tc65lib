/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.sms;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
import hm.sms.PDU;
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
        boolean ok;

        //setting preferred SMS message storage to SIM
        try {
            Thread.sleep(3000); //need 3 seconds from SIM initialization according to docs
        } catch (Exception ex) {
        }
        ok = ATCommands.sendWhileNotOk("AT+CPMS=\"MT\",\"MT\",\"MT\"");
        if (Logger.BUILD_DEBUG) {
            Logger.log("setting preferred SMS message storage to SIM + ME... " + ok);
        }

        //set mode to PDU Mode
        ok = ATCommands.sendWhileNotOk("AT+CMGF=0");
        if (Logger.BUILD_DEBUG) {
            Logger.log("set mode to PDU Mode... " + ok);
        }
        //enable receiving of new SMS URCs
        ATCommands.sendUrc("AT+CNMI=2,1,0,0,1");
    }

    public void stop() {
        ATCommands.removeListener(this);
    }

    public void ATEvent(String ate) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".ATEvent( \"" + ate + "\" )");
        }
        if (ate.indexOf("+CMTI:") >= 0) {                //if it is SMS
            String index = null;
            try {
                index = new String(ate.substring(ate.lastIndexOf(',') + 1));
            } catch (Exception e) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this, e, 102, "Event=" + ate + ",index=" + index);
                }
                return;
            }
            //hint: you can add here some code to indicate somewhere (LED, LCD, ...) about received SMS
            try {
                String incomingSMS = ATCommands.send("AT+CMGR=" + index);
                if (Logger.BUILD_DEBUG) {
                    Logger.log("New SMS:\n" + incomingSMS);
                }
                if (incomingSMS.indexOf("0,,0") >= 0) {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log("Empty record during reading SMS at index " + index);
                    }
                    // possibly due to mem1 != mem3. Sending an AT+CPMS="MT","MT","MT" command is advised.
                    ATCommands.sendWhileNotOk("AT+CPMS=\"MT\",\"MT\",\"MT\"");
                    return;
                }
                if (incomingSMS.indexOf("ERROR") >= 0) {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log("ERROR during reading SMS at index " + index);
                    }
                } else if (incomingSMS.indexOf('\n') >= 0) {
                    String pduString = null;
                    try {
                        pduString = new String(incomingSMS.substring(incomingSMS.indexOf('\n') + 1));
                        pduString = pduString.substring(pduString.indexOf('\n') + 1);
                        pduString = pduString.substring(0, pduString.indexOf('\n'));
                    } catch (Exception ex) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this, ex, 134, "incomingSMS=" + incomingSMS + ",message=" + pduString);
                        }
                        return;
                    }
                    pduString = pduString.trim();
                    PDU pdu = new PDU(pduString);
                    String returnAddress = null;
                    try {
                        returnAddress = pdu.getSenderNumber();
                    } catch (Exception e) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this, e, 145, "message=" + pduString + ",returnAddress=" + returnAddress);
                        }
                    }
                    //hint: here I suggest you can check if you allow SMSs from this address
                    String message;
                    try {
                        message = pdu.getUserData();
                    } catch (Exception e) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log(this, e, 153, "message=" + pduString);
                        }
                        return;
                    }
                    handleSMS(returnAddress, message);
                } else {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log("UNKNOWN ERROR during reading SMS");
                    }
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this, ex, 167);
                }
            }
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
