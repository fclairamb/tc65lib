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
import hm.sms.PDU;
import java.util.Enumeration;
import java.util.Vector;
import org.javacint.at.ATCommands;
import org.javacint.at.ATURCQueueHandler;
import org.javacint.logging.Logger;

/**
 * SMS Receiving class
 */
public class SMSReceiver implements ATCommandListener {

    private final Vector consumers = new Vector();

    private SMSReceiver() {
    }

    public void addConsumer(SMSConsumer c) {
        consumers.addElement(c);    //actually it IS synchronized, so we don't need additional synchronized block
    }

    public void removeConsumer(SMSConsumer c) {
        consumers.removeElement(c);    //actually it IS synchronized, so we don't need additional synchronized block
    }

    /**
     * Each consumer should return true once they consider it is of no other consumer's interest. It works the same with command receivers.
     */
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
     * Start SMS reception.</br>
     * </br>
     * This methold should only be called once all SMS receivers have been called.</br>
     * </br>
     * The idea is to choose when we will want to handle SMS. Because we need to:</br>
     * <ul>
     * <li>Start the ATCommands (this should be done on first ATCommands call, so not a big issue in current design)
     * <li>Start the SMS consumers
     * <li>Register them to the SMS Receiver
     * <li>Start the SMS receiver (and only now)
     * </ul>
     */
    public void start() {
        ATCommands.addListener(new ATURCQueueHandler(this)); // This indirection prevents the URC call from being blocked
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

    /**
     * Stop SMS reception
     */
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
