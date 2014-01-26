package org.javacint.time;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.javacint.common.BufferedReader;

/**
 * Simple HTTP time client.
 *
 * On the server side, a simple timestamp must be returned.
 *
 * For PHP: <?= time ?>
 * For Java: System.out.println( System.currentTimeMillis() / 1000 );
 * For Python: print(int(time.time()))
 */
public class HttpTimeClient implements TimeClient {

    private final String url;

    public HttpTimeClient(String url) {
        this.url = url;
    }

    public long getTime() throws IOException {
        HttpConnection conn = (HttpConnection) Connector.open(url);
        int rc = conn.getResponseCode();
        if (rc == 200) {
            BufferedReader reader = new BufferedReader(conn.openInputStream());
            return Long.parseLong(reader.readLine());
        } else {
            throw new RuntimeException("Server returned " + rc);
        }
    }
}
