package org.javacint.common;

/**
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class Data {

	/**
	 * Default offset.
	 *
	 * Default offset value calculated to easily add data at the beginning of
	 * buffers without triggering new allocation and copy.
	 *
	 * It is very useful to wrap data inside a frame.
	 */
	private static final int MARGIN_BEFORE = 10;
	private static final int MARGIN_AFTER = 10;
	public byte[] buffer;
	public int offset;
	public int length;

	/**
	 * Create a new empty data container.
	 */
	public Data() {
		free();
	}

	/**
	 * Create a new data container around a buffer.
	 *
	 * @param buf Buffer to wrap it around
	 */
	public Data(byte[] buf) {
		buffer = buf;
		offset = 0;
		length = buffer.length;
	}

	/**
	 * Create a new data container around a buffer of a specific size.
	 *
	 * @param size Size of the buffer
	 */
	public Data(int size) {
		prepare(size);
	}

	/**
	 * Create a new data container around a buffer with an offset and a length.
	 *
	 * @param b Buffer
	 * @param o Offset
	 * @param l Length
	 */
	public Data(byte[] b, int o, int l) {
		buffer = b;
		offset = o;
		length = l;
	}

	/**
	 * Create a new data container around the bytes of a string.
	 *
	 * @param str String
	 */
	public Data(String str) {
		buffer = str.getBytes();
		offset = 0;
		length = buffer.length;
	}

	/**
	 * Allocate more memory if needed to be able to add up to size.
	 *
	 * @param size Size
	 */
	public final void prepare(int size) {
		if (buffer != null && buffer.length - offset > size) {
			return;
		}
		size += (MARGIN_BEFORE + MARGIN_AFTER);
		byte[] newBuffer = new byte[size];
		if (buffer != null) {
			System.arraycopy(buffer, offset, newBuffer, MARGIN_BEFORE, length);
		}
		offset = MARGIN_BEFORE;
		buffer = newBuffer;
	}

	/**
	 * Free the buffer.
	 */
	public final void free() {
		buffer = null;
		offset = MARGIN_BEFORE;
		length = 0;
	}

	/**
	 * Add some data to an other buffer.
	 * 
	 * @param data Data to append to the offer buffer
	 */
	public void append(Data data) {
		append(data.buffer, data.offset, data.length);
	}

	public void append(byte[] addBuf, int addOff, int addLen) {
		if (length == 0 || addLen > buffer.length - length - offset) {
			byte[] newBuffer = new byte[MARGIN_BEFORE + length + addLen + MARGIN_AFTER];
			if (length != 0) {
				System.arraycopy(buffer, offset, newBuffer, MARGIN_BEFORE, length);
			}
			buffer = newBuffer;
			offset = MARGIN_BEFORE;
		}
		System.arraycopy(addBuf, addOff, buffer, offset + length, addLen);
		length += addLen;
	}

	public void prepend(Data data) {
		prepend(data.buffer, data.offset, data.length);
	}

	public void prepend(byte[] addBuf, int addOff, int addLen) {
		if (addLen <= offset) {
			offset -= addLen;
			length += addLen;
			System.arraycopy(addBuf, addOff, buffer, offset, addLen);
		} else {
			byte[] previousbuffer = buffer;
			int previousOffset = offset;
			int previousLength = length;

			buffer = addBuf;
			offset = addOff;
			length = addLen;
			append(previousbuffer, previousOffset, previousLength);
		}
	}

	public Data removeBefore(int len) {
		offset += len;
		length -= len;
		return this;
	}

	public Data removeAfter(int len) {
		length -= len;
		return this;
	}

	public Data clone() {
		return new Data(buffer, offset, length);
	}

	public Data copy() {
		Data ins = new Data(new byte[MARGIN_BEFORE + length + MARGIN_AFTER], MARGIN_BEFORE, 0);
		ins.append(this);
		return ins;
	}
}
