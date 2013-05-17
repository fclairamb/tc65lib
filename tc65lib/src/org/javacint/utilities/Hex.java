package org.javacint.utilities;

import java.io.ByteArrayOutputStream;

public final class Hex {

    private static StringBuffer hexCharsBuffer;
    private static ByteArrayOutputStream byteBuffer;
    private static char[] intToHexBuffer;
    private static int b;
    private static int charPosition;
    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Gets a hex-string from a byte array, starting from 'start' till the 'end'
     * @param paramArrayOfByte byte array to be processed
     * @param start position of first byte to be converted
     * @param end position of last byte to be converted + 1
     * @return String containing hex-characters 0-9, A-F, without spaces
     */
    public static synchronized String doHexCharsArray(byte[] paramArrayOfByte, int start, int end) {
        if (hexCharsBuffer == null) {
            hexCharsBuffer = new StringBuffer(end - start * 2 + 2);
        } else {
            hexCharsBuffer.setLength(0);
            hexCharsBuffer.ensureCapacity(end - start * 2 + 2);
        }
        while (start < end) {
            hexCharsBuffer.append(toHexChar(paramArrayOfByte[start] >> 4 & 0x0F)).append(toHexChar(paramArrayOfByte[start] & 0x0F));
            start += 1;
        }
        return hexCharsBuffer.toString();
    }

    /**
     * Gets a hex-string from a byte array
     * @param paramArrayOfByte byte array to be processed
     * @return String containing hex-characters 0-9, A-F, without spaces
     */
    public static String doHexCharsArray(byte[] paramArrayOfByte) {
        return doHexCharsArray(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    /**
     * Get byte array from a string containing hex-characters without spaces, like "1239abcDEF". Odd characters at the end are ignored.
     * @param paramString string to be parsed
     * @return byte array of parsed bytes
     */
    public static synchronized byte[] doHexBytesArray(String paramString) {
        if (byteBuffer == null) {
            byteBuffer = new ByteArrayOutputStream(paramString.length() / 2 + 2);
        } else {
            byteBuffer.reset();
        }
        charPosition = 0;
        try {
            while (charPosition < paramString.length()) {
                b = toHexByte(paramString.charAt(charPosition)) << 4;
                charPosition += 1;
                b |= toHexByte(paramString.charAt(charPosition));
                byteBuffer.write(b);
                charPosition += 1;
            }
        } catch (Exception localException) {
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Returns hex character from integer
     * @param paramInt integer from we'll generate character, only first 4 bits matter
     * @return 0-9, A-F
     */
    public static char toHexChar(int paramInt) {
        return hexChars[(paramInt & 0xF)];
    }

    /**
     * Returns Hex-string with required width from integer with necessary nulls in front. <br>
     * Возвращает Hex строку из целого числа, с предшествующими нулями до нужной длины
     * @param num The integer to be converted to hex string
     * @param w The required width of the returned string
     * @return String <b>Hex-string</b> of required width "w",<br> <b>NULL</b> if "w" is negative
     */
    public static synchronized String intToHexFixedWidth(int num, int w) {
        if (w < 0) {
            return null;
        }
        if (intToHexBuffer == null || intToHexBuffer.length < w) {
            intToHexBuffer = new char[w];
        }
        for (int i = 0; i < w; i++) {
            intToHexBuffer[w - i - 1] = hexChars[((num >>> (4 * i)) & (0x0F))];
        }
        return new String(intToHexBuffer, 0, w);
    }

    /**
     * Returns byte value from hex-presumed character
     * @param paramChar 0-9, a-f, A-F hex character
     * @return 0-15
     */
    public static int toHexByte(char paramChar) {
        if ((paramChar > '/') && (paramChar < ':')) {
            return paramChar - '0';
        }
        if ((paramChar > '@') && (paramChar < 'G')) {
            return paramChar - '7';
        }
        if ((paramChar > '`') && (paramChar < 'g')) {
            return paramChar - 'W';
        }
        return 0;
    }
}

/* Location:           C:\Documents and Settings\PyTh0n\libX700\lib\libX700.jar
 * Qualified Name:     util.Hex
 * JD-Core Version:    0.6.0
 */

