package org.javacint.common;

import java.util.Vector;

/**
 * Vectors handling class.
 *
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
