package org.javacint.common;



import java.io.IOException;
import java.io.OutputStream;

/**
 * Byte array output stream.
 *
 * This class allows to write bytes into a memory buffer. When the buffer is
 * full it grows by two times its current size.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class ByteArrayOutputStream extends OutputStream {

	private int offset = 0;
	private byte data[] = new byte[8];

	public void write(int i) throws IOException {
		if (offset == data.length) {
			changeBufferSize(data.length * 2);
		}
		data[offset++] = (byte) i;
	}

	public byte[] getData() {
		return data;
	}

	public int getSize() {
		return offset;
	}

	public void reset() {
		offset = 0;
	}

	public void reduce() {
		changeBufferSize(offset);
	}

	private void changeBufferSize(int size) {
		byte[] newData = new byte[size];
		System.arraycopy(data, 0, newData, 0, offset);
	}
}
