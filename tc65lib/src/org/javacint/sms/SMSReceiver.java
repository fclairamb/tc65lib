package org.javacint.sms;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Enumeration;
import java.util.Vector;
import org.javacint.at.ATCommandPooled;
import org.javacint.at.ATCommands;
import org.javacint.at.ATURCQueueHandler;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 * SMS Receiving class
 */
public class SMSReceiver implements ATCommandListener {

    private static final Vector consumers = new Vector();
    private static SMSReceiver instance = new SMSReceiver();

    private SMSReceiver() {
    }

    public static void addConsumer(SMSConsumer c) {
        consumers.addElement(c);    //actually it IS synchronized, so we don't need additional synchronized block
    }

    public static void removeConsumer(SMSConsumer c) {
        consumers.removeElement(c);    //actually it IS synchronized, so we don't need additional synchronized block
    }
    private static final boolean LOG_SMS_HANDLING = true;

    /**
     * Each consumer should return true once they consider it is of no other
     * consumer's interest. It works the same with command receivers.
     */
    private boolean handleSMS(String from, String content) {
        synchronized (consumers) {
            for (Enumeration e = consumers.elements(); e.hasMoreElements();) {
                SMSConsumer cons = (SMSConsumer) e.nextElement();
                try {
                    if (LOG_SMS_HANDLING) {
                        Logger.log(cons + ".smsReceived(\"" + from + "\", \"" + content + "\");");
                    }
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
     * Start SMS reception.</br> </br> This methold should only be called once
     * all SMS receivers have been called.</br> </br> The idea is to choose when
     * we will want to handle SMS. Because we need to:</br> <ul> <li>Start the
     * ATCommands (this should be done on first ATCommands call, so not a big
     * issue in current design) <li>Start the SMS consumers <li>Register them to
     * the SMS Receiver <li>Start the SMS receiver (and only now) </ul>
     */
    public static void start() {
        ATCommands.addListener(new ATURCQueueHandler(instance)); // This indirection prevents the URC call from being blocked

        // text mode (PDU mode is too complex for now)
        ATCommands.sendAll("AT+CMGF=1");

        // URC on SMS reception
        ATCommands.sendUrc("AT+CNMI=1,1");

        // We will read up to 20 messages stored
        for (int i = 1; i <= 20; i++) {
            if (!instance.readSms(i)) { // netbeans doesn't like empty for
                break;
            }
        }
    }

    /**
     * Stop SMS reception
     */
    public static void stop() {
        ATCommands.removeListener(instance);
    }

    public void ATEvent(String urc) {
        if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
            Logger.log(this + ".ATEvent( \"" + urc + "\" )");
        }
        if (urc.indexOf("+CMTI:") > 0) {                //if it is SMS
            String sIndex;
            try {
                sIndex = urc.substring(urc.lastIndexOf(',') + 1);
                readSms(Integer.parseInt(sIndex));
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".ATEvent( " + urc + " )", ex, true);
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
    private static final boolean LOG_SMS_RECEPTION = true;

    private void deleteSms(int index) {
        ATCommands.send("AT+CMGD=" + index);
    }

    private boolean readSms(int index) {
        if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
            Logger.log(this + ".readSms( " + index + " );");
        }
        try {
            String cmd = "AT+CMGR=" + index;
            String rawSms = ATCommands.send(cmd);
            if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                Logger.log("Raw: " + rawSms.replace('\r', '%').replace('\n', '$'));
            }
            // We remove the echo
            rawSms = rawSms.substring(cmd.length() + 8);
            if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                Logger.log("Raw: " + rawSms.replace('\r', '%').replace('\n', '$'));
            }
            int pHeader = rawSms.indexOf('\r');
            if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                Logger.log(this + ":pheader = " + pHeader);
            }
            if (pHeader < 10) { // If the line is too short, we don't have anything
                return false;
            }
            String header = rawSms.substring(0, pHeader);
            if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                Logger.log(this + ":header = \"" + header.replace('\r', '%').replace('\n', '$') + "\"");
            }
            String phone;
            {
                String headerArgs[] = Strings.split(',', header);
                phone = headerArgs[1];
                phone = phone.substring(1, phone.length() - 1);
                if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                    Logger.log(this + ":phone = \"" + phone.replace('\r', '%').replace('\n', '$') + "\"");
                }
            }

            String content = rawSms.substring(pHeader + 2, rawSms.length() - 8);
            if (Logger.BUILD_DEBUG && LOG_SMS_RECEPTION) {
                Logger.log(this + ":content = \"" + content.replace('\r', '%').replace('\n', '$') + "\"");
            }

            handleSMS(phone, content);
            return true;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this, ex, 167);
            }
        } finally {
            deleteSms(index);
        }
        return false;
    }
}
