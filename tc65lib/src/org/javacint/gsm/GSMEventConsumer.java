package org.javacint.gsm;

/**
 * GSM event consumer interface.
 */
public interface GSMEventConsumer {

    /**
     * GSM Event was received
     *
     * @param name Name of the event
     * @param value Value of the event
     *
     * Names can be : simlocal, rssi, etc.
     */
    void gsmEventReceived(String name, String value);
}
