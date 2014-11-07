package org.javacint.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.javacint.logging.Logger;

/**
 * Stream copier.
 * This code is not optimized & not tested.
 */
public class StreamCopier implements Runnable {

    private final InputStream is;
    private final OutputStream os;
    private boolean loop = true;

    public StreamCopier(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    public void stop() {
        this.loop = false;
    }

    public void run() {
        while (loop) {
            try {
                int b = is.read();
                if (b == -1) {
                    break;
                }
                os.write(b);
            } catch (IOException ex) {
                if (loop) {
                    if (Logger.BUILD_NOTICE) {
                        Logger.log("StreamCopier", ex);
                    }
                }
                break;
            }
        }
    }
}
