package org.javacint.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.javacint.logging.Logger;

/**
 * Buffered reader.
 */
public class BufferedReader {

	/**
	 * The underlying input stream
	 */
	private final InputStream is;
	/**
	 * The buffer used to generate lines
	 */
	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	/**
	 * Constructor
	 *
	 * @param stream Stream to read from
	 */
	public BufferedReader(InputStream stream) {
		is = stream;
	}

	/**
	 * Read a line
	 *
	 * @return Line to read from a stream
	 */
	public String readLine() {
		try {
			while (true) {
				int c = is.read();

				if (c == '\n' || c == '\r' || (c == -1 && bos.size() > 0)) {
					String str = new String(bos.toByteArray());
					bos.reset();
					return str;
				} else if (c == -1) {
					return null;
				} else {
					bos.write(c);
				}
			}
		} catch (Exception ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log("BasicBufferedReader.readLine", ex);
			}
		}
		return null;
	}

	/**
	 * Close the buffer
	 *
	 * @throws java.io.IOException
	 */
	public void close() throws IOException {
		is.close();
	}
}
