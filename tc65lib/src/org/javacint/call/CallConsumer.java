package org.javacint.call;

/**
 * Call consumer interface
 */
public interface CallConsumer {

    /**
     * Received when the phone is ringing
     *
     * @param phoneNumber Phone number to receive
     */
    void phoneIsRinging(String phoneNumber, int ringNb);
}
