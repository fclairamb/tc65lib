/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javacint.utilities;

import java.util.Date;

/**
 * Logging class
 */
public class Log {

//#if DebugLevel=="debug"
    private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif

    public static void add2Log(String text, Class callerClass){
        if (DEBUG) {
            System.out.println(new Date().toString() + ": " + Thread.currentThread() + " " + callerClass + " " + text);
        }
    }

    public static void println(String text, Object caller){
        if (DEBUG) {
            System.out.println(new Date().toString() + ": " + Thread.currentThread() + " " + caller.getClass() + " " + text);
        }
    }

    public static void println(String text){
        if (DEBUG) {
            System.out.println(new Date().toString() + ": " + Thread.currentThread() + " " + text);
        }
    }
}

