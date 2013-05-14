/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common;

import java.util.Vector;

/**
 *
 * @author florent
 */
public class Vectors {

	/**
	 * Convert a vector to an array of string
	 *
	 * @param vector Vector of string
	 * @return Array of string
	 */
	public static String[] toStringArray(Vector vector) {
		String[] strArray = new String[vector.size()];
		for (int i = 0; i < strArray.length; i++) {
			strArray[i] = (String) (vector.elementAt(i));
		}
		return strArray;
	}
}
