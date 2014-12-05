/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.control.m2mp;

/**
 *
 * @author Florent
 */
public interface M2MPClient extends IProtocolLayerSend {

	void setListener(IProtocolLayerReceive listener);

	void start() throws Exception;

	void stop() throws Exception;

	public void setIdent(String string);

	public void setCapabilities(String echoloc);

	public void sendCapabilities();

	public void setStatus(String name, String value);
}
