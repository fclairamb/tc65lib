/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.utilities;

import java.io.*;
import java.util.Vector;
import org.javacint.at.ATCommands;

/**
 *
 * @author pyth0n
 */

// TODO: We need to move each part to its corresponding class. Most of these
// methods are related to byte manipulation, so we should move them 

public class Utilities {

    /**
     * Returns boolean array with required length from integer with necessary nulls in MSB, or cropped omitting MSB. <br>
     * Возвращает двоичный массив из целого числа, с последующими нулями до нужной длины, либо обрезанную, вырезая MSB.
     * @param i The integer to be converted to boolean array
     * @param w The required length of the returned array
     * @return boolean[] <b>BinaryArray</b> of required length "w",<br> <b>NULL</b> if "w" is negative
     */
    static public boolean[] intToBinaryArrayFixedWidth(int i, int w) {
        if (w < 0) { //Если длина меньше нуля //If the length is negative
            return null; //Возвращаем нуль
        }
        boolean[] t = intToBinaryArray(i); //Создаем сырой массив //Make raw array
        if (t.length > w) { //Если получилась длина больше чем нужно //If the resulting length is bigger than we need
            boolean[] result = new boolean[w];  //Создаем массив нужной длины //Make an array of the required length
            System.arraycopy(t, 0, result, 0, w); //Копируем туда из сырого массива //And copy data there from raw array
            return result; //Готово //Done
        } else if (t.length < w) { //Если получилась длина меньше чем нужно //If the resulting length is less than we need
            boolean[] result = new boolean[w];  //Создаем массив нужной длины //Make an array of the required length
            System.arraycopy(t, 0, result, 0, t.length); //Копируем туда из сырого массива //And copy data there from raw array
            return result; //Готово //Done
        } else { //Если получилось ровно столько, сколько нам нужно //If the resulting length is exactly that we need
            return t; //Готово //Done
        }
    }

    /**
     * Returns reversed string <br>
     * Функция, разворачивающая строку
     * @param s The string to be reversed
     * @return String <b>reversed string</b>,<br><b>NULL</b> if "s" is NULL
     */
    static public String reverseString(String s) {
        String r = new String();
        for (int i = s.length() - 1; i >= 0; i--) {
            r += s.charAt(i);
        }
        return r;
    }

    /**
     * This function splits string "str" to 2 pieces delimited by the first occurence of "delimiter"
     * @param delimiter the delimiter of string
     * @param str the string to be split
     * @return String[] <b>array</b> of 2 pieces of the string delimited by the first occurence of "delimiter",<br><b>NULL</b> if delimiter is not found OR any parameter is NULL or empty
     */
    static public String[] strSplitToArr2(String delimiter, String str) {
        if (delimiter == null || str == null || delimiter.length() == 0 || str.length() == 0) {
            return null;
        }
        int b = str.indexOf(delimiter);
        if (b == -1) {
            return null;
        }
        String[] result = new String[2];
        result[0] = str.substring(0, b);
        result[1] = str.substring(b + delimiter.length());
        return result;
    }

    /**
     * This function splits string "str" to pieces delimited by the "delimiter"<br>The trailing "delimiter" is not required
     * @param delimiter the delimiter of string
     * @param str the string to be split
     * @return String[] <b>array</b> of pieces of the string delimited by the "delimiter",<br>String[1] <b>array</b> if delimiter is not found,<br><b>NULL</b> if any parameter is NULL or empty
     */
    static public String[] strSplitToArr(String delimiter, String str) {
        Vector vArr = new Vector();
        int a = 0;
        int b = 0;

        if (delimiter == null || str == null || delimiter.length() == 0 || str.length() == 0) {
            return null;
        }

        while (b != -1) {
            b = str.indexOf(delimiter, a);
            if (b == -1) {
                vArr.addElement(str.substring(a));
                break;
            }
            vArr.addElement(str.substring(a, b));
            a = b + delimiter.length();
        }

        String[] result = new String[vArr.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) vArr.elementAt(i);
        }
        return result;
    }

    /**
     * Function to convert binary string to boolean array. AS IS, NOT REVERSED!
     * @param binstring The binary string to convert
     * @return boolean[] <b>array</b>,<br><b>NULL</b> if "binstring" contains characters other than '0' and '1'
     */
    static public boolean[] binStringToBooleanArray(String binstring) {
        boolean[] result = new boolean[binstring.length()];
        for (int i = 0; i < binstring.length(); i++) {
            char c = binstring.charAt(i);
            if (c != '1' && c != '0') {
                return null;
            }
            result[i] = (c == '1' ? true : false);
        }
        return result;
    }

    /**
     * Function to convert hex string to byte array. AS IS, NOT REVERSED!
     * @param hexString to convert
     * @return byte array, each byte is from consequent two chars from hex string
     * @throws IllegalArgumentException if hexString is not even
     * @throws NumberFormatException if hexString contains non-hex characters
     */
    static public byte[] hexStringToByteArray(String hexString) throws IllegalArgumentException, NumberFormatException {
        if ((hexString.length() % 2) == 1) {
            throw new IllegalArgumentException("Length of " + hexString + " is not even");
        }
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++) {
            result[i] = (byte) Integer.parseInt(hexString.substring(2 * i, (2 * (i + 1))), 16);
        }
        return result;
    }

    /**
     * Function to convert boolean array to binary string. AS IS, NOT REVERSED!
     * @param bArr The boolean array to convert
     * @return String consist of "0" and "1"
     */
    static public String booleanArrayToBinString(boolean[] bArr) {
        String result = "";
        for (int i = 0; i < bArr.length; i++) {
            result += (bArr[i] ? "1" : "0");
        }
        return result;
    }

    /**
     * Natural Power function, using fast algorythm.
     * @param x integer base
     * @param y positive integer exponent. In case of negative y, returns 0
     * @return natural power x^y
     */
    public static int pow(int x, int y) {
        if (x == 2 && y >= 0) {
            return 1 << y;
        }
        if (y > 6) { //Fast algorythm is faster only when y > 6, otherwise slower.
            return powfast(x, y);
        } else if (y > 1) {
            int result = x;
            for (int i = 1; i < y; i++) {
                result *= x;
            }
            return result;
        } else if (y == 1) {
            return x;
        } else if (y == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Power function
     * @param x double base
     * @param y integer exponent
     * @return x^y
     */
    public static double pow(double x, int y) {
        if (x == 2 && y >= 0) {
            return 1 << y;
        }
        if (y > 6) { //Fast algorythm is faster only when y > 6, otherwise slower.
            return powfast(x, y);
        } else if (y > 1) {
            for (int i = 1; i < y; i++) {
                x *= x;
            }
            return x;
        } else if (y == 1) {
            return x;
        } else if (y < 0) {
            if (x != 0) {
                return (1.0 / pow(x, -y));
            } else {
                return Double.NaN;
            }
        } else {
            return 1;
        }
    }

    /**
     * Fast raising to power. Faster when y > 6, otherwise slower.
     * @param x double base
     * @param y positive integer exponent. Unsafe!
     * @return power x^n
     */
    private static double powfast(double x, int power) {
        int k;
        double res, px;
        boolean firsttime = true;

        // We observe that x ^ power could be written as a product of
        // factors, in which we may find: x ^ 1, x ^ 2, x ^ 4, x ^ 8 ...
        // A factor appears in the product if in the binary representation
        // of "power", the corresponding digit is 1. The result is multiplied
        // with "power", only if the digit is 1. E.g: x ^ 13 = x ^ 8 + x ^ 4 + x ^ 1,
        // because 13 in binary is 1101.
        // To convert the power to binary, we must divide it with 2 at every iteration.
        for (res = 1, px = x, k = power; k > 0; k = k >> 1) {
            // Check if the bit is 1. Only if it is 1 then we
            // multiply res with px
            if ((k & 1) == 1) {
                if (firsttime) {
                    res = px;
                    firsttime = false;
                } else {
                    res *= px;
                }
            }
            px *= px;
        }
        return res;
    }

    /**
     * Fast raising to Natural power. Faster when y > 6, otherwise slower.
     * @param x double base
     * @param y positive integer exponent. Unsafe!
     * @return power x^n
     */
    private static int powfast(int x, int power) {
        int k;
        int res, px;
        boolean firsttime = true;

        // We observe that x ^ power could be written as a product of
        // factors, in which we may find: x ^ 1, x ^ 2, x ^ 4, x ^ 8 ...
        // A factor appears in the product if in the binary representation
        // of "power", the corresponding digit is 1. The result is multiplied
        // with "power", only if the digit is 1. E.g: x ^ 13 = x ^ 8 + x ^ 4 + x ^ 1,
        // because 13 in binary is 1101.
        // To convert the power to binary, we must divide it with 2 at every iteration.
        for (res = 1, px = x, k = power; k > 0; k = k >> 1) {
            // Check if the bit is 1. Only if it is 1 then we
            // multiply res with px
            if ((k & 1) == 1) {
                if (firsttime) {
                    res = px;
                    firsttime = false;
                } else {
                    res *= px;
                }
            }
            px *= px;
        }
        return res;
    }

    /**
     * Helps to know what bit is at some position in some number
     * @param number in which we search a bit
     * @param bitPosition at which we want to know a bit
     * @return bit that is on bitPosition at number
     * @throws IllegalArgumentException if bitPosition is negative
     */
    public static boolean bitAt(int number, int bitPosition) throws IllegalArgumentException {
        if (bitPosition >= 0) {
            return ((number >> bitPosition) & 1) == 1 ? true : false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates a boolean array representation of the integer argument as an unsigned integer in base 2.<br>
     * <br>
     * The unsigned integer value is the argument plus 2^32 if the argument is negative; otherwise it is equal to the argument.
     * This value is converted to a binary array (base 2) with no extra leading 0s.
     * If the unsigned magnitude is zero, it is represented by a single zero character '0' (false);
     * otherwise, the last character of the representation of the unsigned magnitude will not be the zero character.
     * The characters '0' (false) and '1' (true) are used as binary digits.
     * @param n
     * @return the boolean array representation of the unsigned integer value represented by the argument in binary (base 2).
     */
    public static boolean[] intToBinaryArray(int n) {
        int l = 0;
        int nShifted = n;

        while (nShifted != 0) {
            l++;
            nShifted >>>= 1;
        }
        if (l == 0) {
            return new boolean[]{false};
        }
        boolean[] result = new boolean[l];

        for (int i = 0; i
                < l; i++) {
            result[i] = ((n >> i) & 1) == 1 ? true : false;
        }
        return result;
    }

    /**
     * Creates an integer from boolean array treating it as binary number
     * @param arr binary number
     */
    public static int binaryArrayToInt(boolean[] arr) {
        int result = 0;
        int i = 0;

        while (i < arr.length) {
            result += (arr[i] ? 1 << i : 0);
            i++;
        }
        return result;
    }

    /**
     * Creates an integer from subset of integer-to-boolean-array boolean array treating it as binary number
     * @param n the number from which we'll generate new number
     * @param from bit position from which we'll start to calculate
     * @param till bit position on which we'll end to calculate (included)
     * @return
     */
    public static int intFromIntegerSubset(int n, int from, int till) throws IllegalArgumentException {
        int result = 0;
        int i = 0;

        if (from <= till) {
            while (from + i <= till) {
                result += (bitAt(n, from + i) ? 1 << i : 0);
                i++;
            }
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Прогрессивная система ожидания байта на линии.
     * @param is Поток, из которого ожидается байт
     * @param maxTime Степень числа 2. Максимальное время, которое можно ожидать байт, будет (2^maxTime)-1
     * @return первый байт из потока. Если не дождались, тогда -1.
     */
    public static int progressiveWaitByteRead(InputStream is, int maxTime) throws IOException {
        int i = 0;
        while (is.available() == 0 && i <= maxTime - 1) {
            try {
                Thread.sleep((long) Utilities.pow(2, i));
            } catch (InterruptedException ex) {
//                ex.printStackTrace();
            }
            i++;
        }
        if (is.available() > 0) {
            return is.read();
        } else {
            return -1;
        }
    }

    /**
     * Прогрессивная система заполнения буфера байтами из потока.
     * @param is Поток, из которого ожидаются байты
     * @param maxTimeFirst Степень числа 2. Максимальное время, которое можно ожидать первый байт, будет (2^maxTime)-1
     * @param maxTimeSequence Степень числа 2. Максимальное время, которое можно ожидать последующие байты, будет (2^maxTime)-1
     * @param buffer Буффер, в который идет чтение.
     * @return Количество прочтенных байт из потока. Если не дождались, тогда -1.
     */
    public static int progressiveWaitBufferRead(InputStream is, int maxTimeFirst, int maxTimeSequence, byte[] buffer) throws IOException {
        boolean first = true;
        int readBytes = 0;
        int r = 0;
        while (true) {
            if (readBytes == buffer.length) {
                return readBytes;
            }
            r = Utilities.progressiveWaitByteRead(is, first ? maxTimeFirst : maxTimeSequence); //Ожидаем первый байт не более (2^maxTime)-1 ms
            first = false;
            if (r == -1) {
                if (readBytes == 0) {
                    return -1;
                }
                return readBytes;
            }
            buffer[readBytes] = (byte) r;
            readBytes++;
            if (readBytes == buffer.length) {
                return readBytes;
            }
            r = is.read(buffer, readBytes, buffer.length - readBytes);
            if (r != -1) {
                readBytes += r;
            }
        }
    }

    /**
     * Сортировка перемешанного массива строк ("сортировка вставками").
     * @param array массив, который следует отсортировать
     */
    public static void sortArray(String array[]) {
        for (int i = 1; i
                < array.length; i++) {
            String key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j].compareTo(key) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    /**
     * Makes a string consist only of latin characters, speaked like russian
     * @param russianString string with russian characters
     * @return string with latin characters
     */
    public static String fromRussianToTranslit(String russianString) {

        char[] russianAlphabet = new char[]{'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', '°'};
        char[] translitAlphabet = new char[]{'a', 'b', 'v', 'g', 'd', 'e', 'e', 'j', 'z', 'i', 'y', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'f', 'h', 'c', 's', 's', '\'', 'y', '\'', 'e', 'u', 'a', 'A', 'B', 'V', 'G', 'D', 'E', 'E', 'J', 'Z', 'I', 'Y', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U', 'F', 'H', 'C', 'S', 'S', '\'', 'Y', '\'', 'E', 'U', 'A', '\"'};

        return replaceCharacters(russianString, russianAlphabet, translitAlphabet);
    }

    /**
     * Converts a string to lower case (because String.toLowerCase() does not work with russian characters
     * @param givenString string that should be lower-cased
     * @return string with lower case characters
     */
    public static String toLowerCase(String givenString) {
        return replaceCharacters(givenString.toLowerCase(), getUpperCaseAlphabet(), getLowerCaseAlphabet());
    }
    
    /**
     * Converts a string to upper case (because String.toUpperCase() does not work with russian characters
     * @param givenString string that should be upper-cased
     * @return string with upper case characters
     */
    public static String toUpperCase(String givenString) {
        return replaceCharacters(givenString.toUpperCase(), getLowerCaseAlphabet(), getUpperCaseAlphabet());
    }
    
    private static char[] getUpperCaseAlphabet() {
        return new char[]{'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я'};
    }
    
    private static char[] getLowerCaseAlphabet() {
        return new char[]{'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я'};
    }
    
    private static String replaceCharacters(String givenString, char[] fromAlphabet, char[] toAlphabet) {
        int a = -1;
        StringBuffer s = new StringBuffer(givenString);
        for (int i = 0; i < fromAlphabet.length; i++) {
            do {
                a = givenString.indexOf(fromAlphabet[i], a + 1);
                if (a >= 0) {
                    s.setCharAt(a, toAlphabet[i]);
                }
            } while (a != -1);
        }
        return s.toString();
    }

    private static long lastGetTimeMillis = 0;
    private static String lastAlexTime = "2001-01-01 01:01:01";
    public static synchronized String getAlexTime() {
        if ((System.currentTimeMillis() - lastGetTimeMillis) < 1000) {
//            Logger.log("getAlexTime(): now " + System.currentTimeMillis() + ", last was " + lastGetTimeMillis + ", returning " + lastAlexTime);
            return lastAlexTime;
        }
        String mytime = "";
        try {
            mytime = ATCommands.send("AT+CCLK?");
        } catch (Exception ex) {
            return "2001-01-01 01:01:01";
        }
        lastGetTimeMillis = System.currentTimeMillis();
        int a = mytime.indexOf("+CCLK:");
        a = mytime.indexOf('"', a);
        a++;

        int b = mytime.indexOf('"', a);
        mytime = mytime.substring(a, b);
        String atime = mytime.replace('/', '-');
        atime = atime.replace(',', ' ');
        lastAlexTime = "20" + atime;

        return lastAlexTime;
    }

    /**
     * Cleans the vector of duplicate objects and null elements
     * @param vector that should be cleaned
     */
    public static void cleanVector(Vector vector) {
        int i = 0;

        while (i < vector.size()) {
            Object obj = vector.elementAt(i);
            //if object is empty, delete it and try again
            if (obj == null) {
                vector.removeElementAt(i);
                continue;
            }

            //Delete all other objects equal this
            for (int j = i + 1; j < vector.size(); j++) {
                if (obj.equals(vector.elementAt(j))) {
                    //Logger.log("cleanVector: Removing duplicate object: " + vector.elementAt(j));
                    vector.removeElementAt(j);
                    j--;
                }
            }
            i++;
        }
    }

    /**
     * Cleans the vector of object occurencies determined by ==
     * @param vector that should be cleaned
     * @param obj that vector will have no more
     */
    public static void cleanVectorFromObject(Vector vector, Object obj) {
        int i = 0;

        while (i < vector.size()) {
            //if object is found, delete it and try again
            if (vector.elementAt(i) == obj) {
                vector.removeElementAt(i);
                continue;
            }
            i++;
        }
    }

    /**
     * Returns a string representation of a number, cropping length of digits after dot (if present) to specified number of digits
     * @param number double number to convert
     * @param digitsAfterDot limit of number of digits after dot
     * @return String with limited number of digits after dot
     */
    public static String cropDouble(double number, int digitsAfterDot) {
        String numberStr = String.valueOf(number);

        if (numberStr.indexOf('.') >= 0) { //Если там есть точка
            int d = numberStr.indexOf('.');
            if (numberStr.length() - d > digitsAfterDot) {//Если там после точки больше, чем нужно, обрезаем
                numberStr = numberStr.substring(0, numberStr.indexOf('.') + digitsAfterDot + 1);
            }
            while (numberStr.endsWith("0")) { //Убираем с конца нули до точки или другой цифры
                numberStr = numberStr.substring(0, numberStr.length() - 1);
            }
            if (numberStr.endsWith(".")) { //Если там осталась только точка, удаляем ее
                numberStr = numberStr.substring(0, numberStr.length() - 1);
            }
        }
        return numberStr;
    }
    
    /**
     * Вставка нового значения в отсортированный массив
     * @param array отсортированный по возрастанию массив, в который следует вставить новое значение
     * @param newValue новое значение, которое надо вставить в массив
     */
    public static short[] insertInSortedArray(short[] array, short newValue) {
        if (array == null) {
            return new short[]{newValue};
        } else if (array.length == 0) {
            return new short[]{newValue};
        }

        short[] newArray = new short[array.length + 1];                             //Новый массив будет больше на 1
        //Вставляем новое значение
        int newPosition = array.length;                                         //Ищем начиная прямо с самого правого конца
        while (newPosition > 0 && array[newPosition - 1] > newValue) {          //Ищем позицию нового значения
            newPosition--;
        }
        if (newPosition != 0) {                                                  //Если оно не первое, то...
            System.arraycopy(array, 0, newArray, 0, newPosition);               //копируем массив до позиции нового значения
        }
        if (newPosition != array.length) {                                      //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, newPosition, newArray, newPosition + 1, array.length - newPosition);//Копируем массив от позиции нового значения
        }
        newArray[newPosition] = newValue;                                       //Пишем туда новое значение
        return newArray;
    }

    /**
     * Вставка нового значения в отсортированный массив, при этом выкидывая одно старое значение
     * @param array отсортированный по возрастанию массив, в который следует вставить новое значение
     * @param newValue новое значение, которое надо вставить в массив
     * @param oldestValue старое значение, которое надо выкинуть из массива, освободив место для нового
     */
    public static void insertSort_doReplaceInSortedArray(short[] array, short newValue, short oldestValue) {
        if (array == null) {
            return;
        } else if (array.length == 0) {
            return;
        } //Убираем старое значение (освобождаем место для нового
        int oldestPosition = array.length - 1;                                  //Ищем с конца в начало
        while (oldestPosition > 0 && array[oldestPosition] != oldestValue) {    //Ищем позицию, где старое значение
            oldestPosition--;
        }
        if (oldestPosition != array.length - 1) {                               //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, oldestPosition + 1, array, oldestPosition, array.length - oldestPosition - 1);//Сдвигаем массив на старое значение (задавливаем его)
        } //Вставляем новое значение
        int newPosition = array.length - 1;                                     //В результате предыдущей операции одно значение массива справа оказалось лишним
        while (newPosition > 0 && array[newPosition - 1] > newValue) {          //Ищем позицию нового значения
            newPosition--;
        }
        if (newPosition != array.length - 1) {                                  //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, newPosition, array, newPosition + 1, array.length - newPosition - 1);//Раздвигаем массив от позиции нового значения
        }
        array[newPosition] = newValue;                                          //Пишем туда новое значение
    }

    /**
     * Вставка нового значения в отсортированный массив
     * @param array отсортированный по возрастанию массив, в который следует вставить новое значение
     * @param newValue новое значение, которое надо вставить в массив
     */
    public static float[] insertInSortedArray(float[] array, float newValue) {
        if (array == null) {
            return new float[]{newValue};
        } else if (array.length == 0) {
            return new float[]{newValue};
        }

        float[] newArray = new float[array.length + 1];                             //Новый массив будет больше на 1
        //Вставляем новое значение
        int newPosition = array.length;                                         //Ищем начиная прямо с самого правого конца
        while (newPosition > 0 && array[newPosition - 1] > newValue) {          //Ищем позицию нового значения
            newPosition--;
        }
        if (newPosition != 0) {                                                  //Если оно не первое, то...
            System.arraycopy(array, 0, newArray, 0, newPosition);               //копируем массив до позиции нового значения
        }
        if (newPosition != array.length) {                                      //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, newPosition, newArray, newPosition + 1, array.length - newPosition);//Копируем массив от позиции нового значения
        }
        newArray[newPosition] = newValue;                                       //Пишем туда новое значение
        return newArray;
    }

    /**
     * Вставка нового значения в отсортированный массив, при этом выкидывая одно старое значение
     * @param array отсортированный по возрастанию массив, в который следует вставить новое значение
     * @param newValue новое значение, которое надо вставить в массив
     * @param oldestValue старое значение, которое надо выкинуть из массива, освободив место для нового
     */
    public static void insertSort_doReplaceInSortedArray(float[] array, float newValue, float oldestValue) {
        if (array == null) {
            return;
        } else if (array.length == 0) {
            return;
        } //Убираем старое значение (освобождаем место для нового
        int oldestPosition = array.length - 1;                                  //Ищем с конца в начало
        while (oldestPosition > 0 && array[oldestPosition] != oldestValue) {    //Ищем позицию, где старое значение
            oldestPosition--;
        }
        if (oldestPosition != array.length - 1) {                               //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, oldestPosition + 1, array, oldestPosition, array.length - oldestPosition - 1);//Сдвигаем массив на старое значение (задавливаем его)
        } //Вставляем новое значение
        int newPosition = array.length - 1;                                     //В результате предыдущей операции одно значение массива справа оказалось лишним
        while (newPosition > 0 && array[newPosition - 1] > newValue) {          //Ищем позицию нового значения
            newPosition--;
        }
        if (newPosition != array.length - 1) {                                  //Если оно не последнее, то надо сдвигать массив
            System.arraycopy(array, newPosition, array, newPosition + 1, array.length - newPosition - 1);//Раздвигаем массив от позиции нового значения
        }
        array[newPosition] = newValue;                                          //Пишем туда новое значение
    }

}

