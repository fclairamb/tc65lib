package org.javacint.io;

import java.io.InputStream;
import java.io.OutputStream;

// TODO: I think we need to remove or rename this class because:
// - It has the same name as a JME class
// - It still requires to use a Connection / ConnectionProfile pair, which is 
// confusing (because not actually generic) and doesn't make life much easier.
// It think we need to rewrite that to refocus on the StreamConnection interface.
/**
 * Defines basic methods to interact with the process
 */
public abstract class Connection {

    /**
     * Generic open method
     *
     * @param cp this connection profile with desired parameters
     * @return true if the connection has been established<br> <b>Should check
     * this and not open the streams unless true is returned</b>
     */
    public abstract boolean open(ConnectionProfile cp);

    /**
     * generic close method
     */
    public void close() {
        try {
            if (this.is != null) {
                this.is.close();
                this.is = null;
            }
            if (this.os != null) {
                this.os.close();
                this.os = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * @return InputStream of this process<br> <b>The method doesen't throw
     * anything so watch for null pointer exception</b>
     */
    public InputStream getInputStream() {
        return is;
    }
    protected InputStream is;

    /**
     * @return OutputStream of this process<br> <b>The method doesen't throw
     * anything so watch for null pointer exception</b>
     */
    public OutputStream getOutputStream() {
        return os;
    }
    protected OutputStream os;
}
