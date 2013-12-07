package org.javacint.control.m2mp;

import java.util.Vector;

/**
 * Protocol layer interface
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public interface IProtocolLayer extends IProtocolLayerSend {

	// S stands for Send    (client --> server)
	// R stands for Receive (server --> client)
	// Basic messages
	/**
	 * Send identification message
	 */
	final byte NET_S_IDENT = 0x01;
	/**
	 * Receive dentification result message
	 */
	final byte NET_R_IDENT_RESULT = 0x01;
	/**
	 * Send acknowledge request
	 */
	final byte NET_S_ACK_REQUEST = 0x02;
	/**
	 * Send acknowledge request reponse
	 */
	final byte NET_S_ACK_RESPONSE = 0x03;
	/**
	 * Receive acknowledge response
	 */
	final byte NET_R_ACK_RESPONSE = 0x02;
	/**
	 * Receive acknowledge request
	 */
	final byte NET_R_ACK_REQUEST = 0x03;
	/**
	 * Send named channel definition
	 */
	final byte NET_S_NC_DEF = 0x20;
	/**
	 * Receive named channel definition
	 */
	final byte NET_R_NC_DEF = 0x20;
	/**
	 * Send data on a named channel
	 */
	final byte NET_S_NC_DATA = 0x21;
	/**
	 * Receive data on a named channel
	 */
	final byte NET_R_NC_DATA = 0x21;
	/**
	 * Send an array of data on a named channel
	 */
	final byte NET_S_NC_DATAARRAY = 0x22;
	/**
	 * Receive an array of data on a named channel
	 */
	final byte NET_R_NC_DATAARRAY = 0x22;
	/**
	 * Send a large data on a named channel
	 */
	final byte NET_S_NC_DATA_LARGE = 0x41;
	/**
	 * Receive a large data on a named channel
	 */
	final byte NET_R_NC_DATA_LARGE = 0x41;
	/**
	 * Send an array of large data on a named channel
	 */
	final byte NET_S_NC_DATAARRAY_LARGE = 0x42;
	/**
	 * Send an array of large data on a named channel
	 */
	final byte NET_R_NC_DATAARRAY_LARGE = 0x42;
	/**
	 * Max value of the special messages
	 */
	final byte NET_MSG_SPECIALIZED_MAX = 0x20;
	/**
	 * Max value of the 1 byte sized messages
	 */
	final byte NET_MSG_1BYTESIZED_MAX = 0x40;
	/**
	 * Max value of the 2 bytes sized messages
	 */
	final byte NET_MSG_2BYTESSIZED_MAX = 0x60;
	/**
	 * Max value of the 4 bytes sized messages
	 */
	final byte NET_MSG_4BYTESSIZED_MAX = (byte) 0x80;

	/**
	 * Called when we are disconnected
	 */
	public void disconnected();

	/**
	 * Treat data received from the network low level class
	 *
	 * This should be called from the low level layer
	 *
	 * @param frame Frame received
	 */
	public void receivedFrame(byte[] frame);

}
