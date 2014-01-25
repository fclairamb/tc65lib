package org.javacint.time;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Simple HTTP Header time client.
 *
 * Gets time (GMT) from the "Date" HTTP header by performing a HEAD request.
 */
public class HttpHeaderTimeClient implements TimeClient {

    private final String url;

    /**
     * Constructor.
     *
     * @param url URL to get the time from.
     */
    public HttpHeaderTimeClient(String url) {
        this.url = url;
    }

    public long getTime() throws IOException {
        HttpConnection conn = null;
        try {
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.HEAD);
            long time = conn.getDate();
            if (time != 0) {
                return time / 1000;
            } else {
                throw new RuntimeException("Server returned no valid date in http header");
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}