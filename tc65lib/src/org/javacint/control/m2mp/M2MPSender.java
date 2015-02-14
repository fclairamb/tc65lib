package org.javacint.control.m2mp;

import java.util.Vector;

public interface M2MPSender {
	
	/**
	 * Send some data
	 *
	 * @param channelName Name of the channel
	 * @param data Data to send
	 */
	public void sendData(String channelName, byte[] data);

	/**
	 * Send some data
	 *
	 * @param channelName Name of the channel
	 * @param data Data to send
	 */
	public void sendData(String channelName, String data);

	/**
	 * Send some data using the channel id directly
	 *
	 * @param channelId Channel Id
	 * @param data Data to send
	 */
	public void sendData(byte channelId, byte[] data);

	/**
	 * Send some byte array data
	 *
	 * @param channelName Name of the channel
	 * @param data Data to send
	 */
	public void sendData(String channelName, byte[][] data);

	/**
	 * Send some string array data
	 *
	 * @param channelName Name of the channel
	 * @param data Data to send
	 */
	public void sendData(String channelName, String[] data);

	/**
	 * Send some String array data
	 *
	 * @param channelName Name of the channel
	 * @param data Data to send as String array
	 */
	public void sendData(String channelName, Vector data);

	/**
	 * Send some byte array data using the channel id directly
	 *
	 * @param channelId Id of the channel to use
	 * @param data Data to send
	 */
	public void sendData(byte channelId, byte[][] data);

	/**
	 * Get the last time some data was received
	 *
	 * @return Time of last data reception
	 */
	//public long getLastRecvTime();

	/**
	 * Get the last time some data was sent
	 *
	 * @return Time of last data sent
	 */
	public long getLastSendTime();

	/**
	 * Send acknowledge response
	 *
	 * @param b acknowledge number
	 */
	public void sendAckResponse(byte b);

	/**
	 * Send acknowledge request
	 *
	 * @param b acknowledge number
	 */
	public void sendAckRequest(byte b);
}
