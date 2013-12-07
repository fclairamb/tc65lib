/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.i2c.mma7660fc;

/**
 *
 * @author Florent
 */
public class Polarity {

	private int nb;

	public Polarity(int nb) {
		this.nb = nb;
	}
	public static final byte Unknown = 0;
	public static final byte Left = 1;
	public static final byte Right = 2;
	public static final byte Down = 5;
	public static final byte Up = 6;

	public int getValue() {
		return nb;
	}

	public String toString() {
		switch (nb) {
			case Left:
				return "Left";
			case Right:
				return "Right";
			case Down:
				return "Down";
			case Up:
				return "Up";
			case Unknown:
				return "Unknown";
			default:
				return nb + " unsupported !";
		}
	}
}
