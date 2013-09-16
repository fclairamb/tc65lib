package org.javacint.demo;

import org.javacint.console.ConsoleBySetting;

/**
 * Global class. Only useful for the few classes that are shared everywhere and
 * not implemented as static classes.
 *
 * We only store the console, because we could have multiple console (one for
 * each port, one for incoming connection on a TCP server, etc.)
 *
 */
public class Global {

    static ConsoleBySetting console;
}
