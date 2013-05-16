/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.atwrap.loading;

/**
 * Named runnabled. Should be used to identify different startup tasks.
 *
 
 */
public abstract class NamedRunnable {

	private final String name;

	public NamedRunnable() {
		this.name = "!!! DEFINE A NAME !!!";
	}

	public NamedRunnable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract void run() throws Exception;
}
