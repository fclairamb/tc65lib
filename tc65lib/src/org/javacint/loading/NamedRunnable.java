/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.loading;

/**
 * Named runnabled. Should be used to identify different startup tasks.
 *
 */
public abstract class NamedRunnable {

    private final String name;

//    public NamedRunnable() {
//        this.name = "!!! DEFINE A NAME !!!";
//    }

    public NamedRunnable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Run some code. This is not a Runnable method as it can throw an
     * exception.
     *
     * @throws Exception
     */
    public abstract void run() throws Exception;
}
