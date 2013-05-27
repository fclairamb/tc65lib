/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.sms;

/**
 * Sample SMS consumer class
 */
public class PingSMSConsumer implements SMSConsumer {

    public boolean smsReceived(String from, String content) {
        if (content.startsWith("ping ")) {
            SimpleSMS.send(from, "pong " + content.substring(5));
            return true;
        }
        return false;
    }
}
