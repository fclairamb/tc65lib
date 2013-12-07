/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.i2c.mma7660fc;

/**
 *
 * @author Florent
 */
public class BackFront {

	private int nb;

	public BackFront(int nb) {
		this.nb = nb;
	}
	public static final byte Unknown = 0;
	public static final byte Front = 1;
	public static final byte Back = 2;

	public int getValue() {
		return this.nb;
	}

	public String toString() {
		switch (nb) {
			case Front:
				return "Front";
			case Back:
				return "Back";
			case Unknown:
				return "Unknown";
			default:
				return nb + " unsupported !";
		}
	}
}
