/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.demo;

import org.javacint.logging.Logger;
import org.javacint.sms.SMSConsumer;

/**
 *
 * @author florent
 */
public class SMSHandler implements SMSConsumer {

    public boolean smsReceived(String from, String content) {
        Logger.log("SMSHandler: " + from + ", " + content);
        return false;
    }
}
