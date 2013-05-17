/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.utilities;

/**
 * The ideal Singleton pattern template
 */
public final class Singleton {

    // <Singleton pattern>
    private static Singleton instance;

    private Singleton() {
    }

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
    // </Singleton pattern>

}

