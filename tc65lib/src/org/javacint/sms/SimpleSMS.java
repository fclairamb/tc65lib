package org.javacint.sms;

import org.javacint.at.ATCommandPooled;
import org.javacint.at.ATCommands;
import org.javacint.logging.Logger;

/**
 * The most basic SMS handling class possible.
 */
public class SimpleSMS {

    private static final char CTRL_Z = 0x1a;
    private static final boolean DEBUG = true;

    public static boolean send(String phone, String content) {
        return send(new Message(phone, content));
    }

    public static boolean send(Message msg) {
        try {
            String ret;
            ATCommandPooled acp = null;
            try {
                acp = ATCommands.getATCommand();
                acp.send("at+cmgf=1");
                ret = acp.send("at+cmgs=" + msg.getPhone());
                ret += acp.send(msg.getContent() + CTRL_Z);
            } finally {
                acp.release();
            }

            if (Logger.BUILD_DEBUG && DEBUG) {
                Logger.log("SMSManagement.send.ret = \"" + ret + "\"");
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("SMSManagement.send", ex, true);
            }
            return false;
        }

        return true;
    }
}
