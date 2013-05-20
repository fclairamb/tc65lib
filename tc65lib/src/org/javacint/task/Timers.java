/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.task;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timers wrapping class.
 *
 * Fast tasks need and known to execute quickly. Slow tasks can be executed
 * slowly.
 *
 * <br />
 *
 * Fast tasks should execute in less than 2s.
 */
public class Timers {

    private static final Timer fast;
    private static final Timer slow;

    static {
        fast = new Timer();
        slow = new Timer();
        fast.schedule(new TimerTask() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }
        }, 0);
        slow.schedule(new TimerTask() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            }
        }, 0);
    }

    public static Timer getFast() {
        return fast;
    }

    public static Timer getSlow() {
        return slow;
    }
}
