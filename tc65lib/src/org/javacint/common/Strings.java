package org.javacint.common;

import java.util.Vector;

/**
 * Strings handling methods.
 */
public class Strings {

    /**
     * Splits a string into multiple strings
     *
     * @param separator Separator char
     * @param source_string Source string
     * @return Array of strings
     *
     * source :
     * http://www.particle.kth.se/~lindsey/JavaCourse/Book/Code/P3/Chapter24/SNAP/Worker.java
     */
    public static String[] split(char separator, String source_string) {

        // First get rid of whitespace at start and end of the string
        String string = source_string.trim();
        // If string contains no tokens, return a zero length array.
        if (string.length() == 0) {
            return (new String[0]);
        }

        // Use a Vector to collect the unknown number of tokens.
        Vector token_vector = new Vector();
        String token;
        int index_a = 0;
        int index_b;

        // Then scan through the string for the tokens.
        while (true) {
            index_b = string.indexOf(separator, index_a);
            if (index_b == -1) {
                token = string.substring(index_a);
                token_vector.addElement(token);
                break;
            }
            token = string.substring(index_a, index_b);
            token_vector.addElement(token);
            index_a = index_b + 1;
        }

        return Vectors.toStringArray(token_vector);

    } // split

    public static String[] split(String separator, String str) {
        Vector nodes = new Vector();

        int index = str.indexOf(separator);
        while (index >= 0) {
            nodes.addElement(str.substring(0, index));
            str = str.substring(index + separator.length());
            index = str.indexOf(separator);
        }
        nodes.addElement(str);

        String[] result = new String[nodes.size()];
        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); loop++) {
                result[loop] = ((String) nodes.elementAt(loop));
            }
        }

        return result;
    }

    /**
     * Returns reversed string
     *
     * @param s The string to be reversed
     * @return String <b>reversed string</b>
     */
    static public String reverse(String s) {
        StringBuffer r = new StringBuffer();
        for (int i = s.length() - 1; i >= 0; i--) {
            r.append(s.charAt(i));
        }
        return r.toString();
    }
}
