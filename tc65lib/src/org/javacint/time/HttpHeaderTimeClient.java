/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.time;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Simple HTTP Header time client.
 *
 *Gets time (GMT) from HTTP header.
 */
public class HttpHeaderTimeClient implements TimeClient {

    private final String url;

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
                return time;
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