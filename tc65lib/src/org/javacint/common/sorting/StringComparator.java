/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common.sorting;

/**
 * String comparator (to allow string sorting)
 * @author Florent Clairambault / www.webingenia.com
 */
public class StringComparator implements Comparator {
	public int compare(Object a, Object b) {
		return a.toString().compareTo(b.toString() );
	}
}
