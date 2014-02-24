package org.javacint.sms;

/**
 * Sample SMS consumer class
 */
public class PingSMSConsumer implements SMSConsumer {

    public boolean smsReceived(Message msg) {
        if (msg.getContent().startsWith("ping ")) {
            SimpleSMS.send(msg.getPhone(), "pong " + msg.getContent().substring(5));
            return true;
        }
        return false;
    }
}
