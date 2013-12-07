package org.javacint.control.m2mp;



/**
 * Network low level layer (TCP) interface
 * @author Florent Clairambault / www.webingenia.com
 */
interface ISocketLayer {

	/**
	 * Send a frame instantly (add it to the top of the FIFO emission queue)
	 * @param data frame to send
	 */
	public void sendFrameFirst( byte[] data );

	/**
	 * Send a frame 
	 * @param data frame to send
	 */
	public void sendFrame( byte[] data );

	/**
	 * Set the protocol layer for data reception
	 * @param proto protocol layer to use
	 */
	public void setProtoLayer( IProtocolLayer proto );
}
