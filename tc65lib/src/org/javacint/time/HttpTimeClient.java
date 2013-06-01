/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.time;

import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.javacint.common.BufferedReader;
import org.javacint.logging.Logger;

/**
 * Simple HTTP time client.
 * 
 * On the server side, a simple timestamp must be returned.
 * 
 * For PHP:
 * <?= time ?>
 * 
 */
public class HttpTimeClient extends TimerTask {

    private final String url;

    public HttpTimeClient(String url) {
        this.url = url;
    }

    public void run() {
        try {
            HttpConnection conn = (HttpConnection) Connector.open(url);
            if ( conn.getResponseCode() == 200 ) {
                BufferedReader reader = new BufferedReader( conn.openInputStream() );
                long time = Long.parseLong( reader.readLine() );
                DateManagement.setCurrentTime(time);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".run", ex, true);
            }
        }
    }
}
