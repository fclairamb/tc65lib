/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common.sorting;

import java.util.Vector;

/**
 * Object sorter.
 *
 * This sorter use the Comparator interface to sort elements. It's first
 * implementation.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class Sorter {

    private final Comparator comparator;

    public Sorter(Comparator comp) {
        comparator = comp;
    }

    public Sorter() {
        comparator = new StringComparator();
    }

    /**
     * Sort an array of objects
     *
     * @param array Array of objects
     * @return the same array instance with some sorted element
     *
     * Please note that this sorting method is not efficient.
     */
    public Object[] sort(Object[] array) {
        boolean changed = true;

        // Very slow sorting mecanism
        while (changed) {
            changed = false;
            for (int j = 0; j < (array.length - 1); j++) {
                Object a = array[j];
                Object b = array[j + 1];
                if (comparator.compare(a, b) > 0) {
                    changed = true;
                    array[j] = b;
                    array[j + 1] = a;
                }
            }
        }

        return array;
    }

    public Vector sort(Vector v) {
        Object[] array = new Object[v.size()];
        for (int i = 0; i < v.size(); i++) {
            array[i] = v.elementAt(i);
        }
        sort(array);

        Vector nv = new Vector(v.size());
        for (int i = 0; i < array.length; i++) {
            nv.addElement(array[i]);
        }
        return nv;
    }
}
