/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javacint.common;

import org.javacint.utilities.Log;

/**
 *
 * @author pyth0n
 */
public class ExceptionHandler {

//#if DebugLevel=="debug"
    private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif

    public static void processException(Object from, Throwable ex, int line) {
        processException(from, ex, line, null);
    }

    public static void processException(Object from, Throwable ex, int line, String comment) {
        if (DEBUG) {
            printException(from, ex, line, comment);
        }
        //GPRS.getInstance().addTextToQueue(GPRS.FAIL_OPEN + from.getClass().getName() + " " + from + ": " + line + "<br />" + (ex == null ? "" : ex.toString()) + (comment == null ? "" : "<br />" + comment) + GPRS.FAIL_CLOSE);
    }

    public static void printException(Object from, Throwable ex, int line) {
        if (DEBUG) {
            printException(from, ex, line, null);
        }
    }

    public static void printException(Object from, Throwable ex, int line, String comment) {
        if (DEBUG) {
            Log.println("EXCEPTION! " + from.getClass().getName() + " " + from + ": " + line + "\n" + (ex == null ? "" : ex.toString()) + (comment == null ? "" : "\n" + comment));
            ex.printStackTrace();
        }
    }
}

