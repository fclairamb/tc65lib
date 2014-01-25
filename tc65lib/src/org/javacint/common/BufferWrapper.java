package org.javacint.common;

/**
 * Buffer wrapper class.
 * This class brings buffer slicing mechanisms.
 * The main goals are:
 * <ul>
 * <li>Reduce memory footprint: We share an instance containg the buffer. Thus
 * is it is released, no instances of the buffer in the lower stack traces will
 * be kept (except if they did a copy).</li>
 * <li>Make faster and simpler buffer manipulation: The buffer has few bytes
 * pre-allocated to allow to add some headers and footers to an existing
 * buffer.</li>
 * </ul>
 */
public class BufferWrapper {

    /**
     * Default offset.
     *
     * Default offset value calculated to easily add data at the beginning of
     * buffers without triggering new allocation and copy.
     *
     * It is very useful to wrap data inside a frame.
     */
    private static final int MARGIN_BEFORE = 10;
    /**
     * Default additional space at the end of the buffer.
     */
    private static final int MARGIN_AFTER = 10;
    public byte[] buffer;
    public int offset;
    public int length;

    /**
     * Create a new empty data container.
     */
    public BufferWrapper() {
        free();
    }

    /**
     * Create a new data container around a buffer.
     *
     * @param buf Buffer to wrap it around
     */
    public BufferWrapper(byte[] buf) {
        buffer = buf;
        offset = 0;
        length = buffer.length;
    }

    /**
     * Create a new data container around a buffer of a specific size.
     *
     * @param size Size of the buffer
     */
    public BufferWrapper(int size) {
        prepare(size);
    }

    /**
     * Create a new data container around a buffer with an offset and a length.
     *
     * @param b Buffer
     * @param o Offset
     * @param l Length
     */
    public BufferWrapper(byte[] b, int o, int l) {
        buffer = b;
        offset = o;
        length = l;
    }

    /**
     * Create a new data container around the bytes of a string.
     *
     * @param str String
     */
    public BufferWrapper(String str) {
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
     * Add some data at the end of the buffer.
     *
     * @param data Data to append to the offer buffer
     */
    public void append(BufferWrapper data) {
        append(data.buffer, data.offset, data.length);
    }

    /**
     * Add some data at the end of the buffer.
     *
     * @param addBuf Buffer
     * @param addOff Offset
     * @param addLen Length
     */
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

    /**
     * Add some data at the beginning of the buffer.
     *
     * @param data Data to add
     */
    public void prepend(BufferWrapper data) {
        prepend(data.buffer, data.offset, data.length);
    }

    /**
     * Add some data at the beginning of the buffer.
     *
     * @param addBuf Buffer
     * @param addOff Offset
     * @param addLen Length
     */
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

    /**
     * Remove some bytes at the beginning of the buffer.
     *
     * @param len Number of bytes to remove
     * @return BufferWrapper instance (for chaining)
     */
    public BufferWrapper removeBefore(int len) {
        offset += len;
        length -= len;
        return this;
    }

    /**
     * Remove some bytes at the end of the buffer
     *
     * @param len Number of bytes to remove
     * @return BufferWrapper instance (for chaining)
     */
    public BufferWrapper removeAfter(int len) {
        length -= len;
        return this;
    }

    /**
     * Clone a buffer.
     * This copies the instance. Adding new data is likely to affect the 
     * previous buffer.
     *
     * @return new BufferWrapper instance with the same buffer
     */
    public BufferWrapper clone() {
        return new BufferWrapper(buffer, offset, length);
    }

    /**
     * Copy a buffer.
     * This copies the instance of the buffer and copies its buffer. It's an
     * independant instance.
     * @return BufferWraper instance with a new buffer
     */
    public BufferWrapper copy() {
        BufferWrapper ins = new BufferWrapper(new byte[MARGIN_BEFORE + length + MARGIN_AFTER], MARGIN_BEFORE, 0);
        ins.append(this);
        return ins;
    }
}
