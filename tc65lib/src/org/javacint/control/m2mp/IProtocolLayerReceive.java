package org.javacint.control.m2mp;

/**
 * Application layer interface
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public interface IProtocolLayerReceive {

	/**
	 * The result of the identification attempt
	 *
	 * @param identified If we could be identified
	 */
	void receivedIdentificationResult(boolean identified);

	/**
	 * Called when an acknowledge response is received
	 *
	 * @param b Acknowledge number
	 */
	void receivedAckResponse(byte b);

	/**
	 * Called when an acknowledge request ist received
	 *
	 * @param b Acknowledge number
	 */
	void receivedAckRequest(byte b);

	/**
	 * Called when some data is received
	 *
	 * @param channelName Channel name
	 * @param data Data received
	 */
	void receivedData(String channelName, byte[] data);

	/**
	 * Called when some data is received
	 *
	 * @param channelName Channel name
	 * @param data Data received
	 */
	void receivedData(String channelName, byte[][] data);

	/**
	 * Called when a command is received
	 *
	 * @param cmdId Command id
	 * @param argv Arguments
	 */
	void receivedCommand(String cmdId, String[] argv);

	/**
	 * We got disconnected.
	 */
	void disconnected();
}
