/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common;

import java.util.TimerTask;

/**
 * 
 * Wraps a Runnable class into a TimerTask class.
 * 
 * This class shouldn't be used.
 * 
 * @author Florent Clairambault / www.webingenia.com
 */
public class TimerTaskProxy extends TimerTask {

	private final Runnable runnable;
	
	public TimerTaskProxy( Runnable runnable ) {
		this.runnable = runnable;
	}
	
	public void run() {
		runnable.run();
	}
	
}
