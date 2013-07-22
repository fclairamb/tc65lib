package org.javacint.time;

/**
 * Time client interface.
 */
public interface TimeClient {
    /**
     * Get the time from a server.
     * @return UNIX timestamp
     */
    long getTime() throws Exception;
}
