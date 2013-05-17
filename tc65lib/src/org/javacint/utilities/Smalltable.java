package org.javacint.utilities;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class implements a memory-restricted version of a Hashtable, which maps keys to values. Any non-<b>null</b> object can be used as a key or as a value.<br />
 * <br />
 * To successfully store and retrieve objects from a hashtable, the objects used as keys must implement the hashCode method (not actually in Smalltable) and the equals method.<br />
 * <br />
 * An instance of Smalltable has the less possible memory footprint, with slowest performance on CRUD operations<br />
 * <br />
 * This example creates a table of numbers. It uses the names of the numbers as keys:<br />
 * <br />
 *          Smalltable numbers = new Smalltable();<br />
 *          numbers.put("one", new Integer(1));<br />
 *          numbers.put("two", new Integer(2));<br />
 *          numbers.put("three", new Integer(3));<br />
 * <br />
 * To retrieve a number, use the following code:<br />
 * <br />
 *          Integer n = (Integer)numbers.get("two");<br />
 *          if (n != null) {<br />
 *              System.out.println("two = " + n);<br />
 *          }<br />
 */
public class Smalltable {

    private Object[] valueArray;
    private Object[] keyArray;

    /**
     * Constructs a new, empty hashtable with a default capacity 0 and load factor 1.
     */
    public Smalltable() {
        this.delete();
    }

    /**
     * Clears this hashtable so that it contains no keys.
     */
    public void clear() {
        synchronized (this) {
            delete();
        }
    }

    private void delete() {
        valueArray = new Object[0];
        keyArray = new Object[0];
    }

    /**
     * Tests if some key maps into the specified value in this hashtable.<br />
     * This operation is more expensive than the containsKey method (not actually in Smalltable).
     * @param value a value to search for.
     * @return <b>true</b> if some key maps to the <b>value</b> argument in this hashtable; <b>false</b> otherwise. 
     */
    public boolean contains(Object value) {
        if (value != null) {
            synchronized (this) {
                for (int j = 0; j < valueArray.length; j++) {
                    if (valueArray[j].equals(value)) {
                        return true;
                    }
                }
            }
        } else {
            throw new NullPointerException("value or key is null");
        }
        return false;
    }

    /**
     * Tests if the specified object is a key in this hashtable. 
     * @param key possible key. 
     * @return <b>true</b> if the specified object is a key in this hashtable;<br />
     * <b>false</b> otherwise.
     */
    public boolean containsKey(Object key) {
        synchronized (this) {
            for (int j = 0; j < keyArray.length; j++) {
                if (keyArray[j].equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped in this hashtable. 
     * @param key a key in the hashtable. 
     * @return the value to which the key is mapped in this hashtable;<br />
     * <b>null</b> if the key is not mapped to any value in this hashtable.
     */
    public Object get(Object key) {
        synchronized (this) {
            for (int j = 0; j < keyArray.length; j++) {
                if (keyArray[j].equals(key)) {
                    return valueArray[j];
                }
            }
        }
        return null;
    }

    /**
     * Tests if this hashtable maps no keys to values. 
     * @return <b>true</b> if this hashtable maps no keys to values;<br />
     * <b>false</b> otherwise.
     */
    public boolean isEmpty() {
        return keyArray.length == 0;
    }

    /**
     * Maps the specified key to the specified value in this hashtable. Neither the key nor the value can be null.<br />
     * The value can be retrieved by calling the get method with a key that is equal to the original key. 
     * @param key the hashtable key.
     * @param value the value. 
     * @return the previous value of the specified key in this hashtable, or <b>null</b> if it did not have one.
     * @throws NullPointerException if the key or value is <b>null</b>.
     */
    public Object put(Object key, Object value) {
        if ((value != null) && (key != null)) {
            synchronized (this) {
                for (int i = 0; i < keyArray.length; i++) {
                    if (keyArray[i].equals(key)) {
                        Object previousValue = valueArray[i];
                        valueArray[i] = value;
                        return previousValue;
                    }
                }
                int length = valueArray.length;
                Object[] newValueArray = new Object[length + 1];
                System.arraycopy(valueArray, 0, newValueArray, 0, length);
                newValueArray[length] = value;

                Object[] newKeyArray = new Object[length + 1];
                System.arraycopy(keyArray, 0, newKeyArray, 0, length);
                newKeyArray[length] = key;
                valueArray = newValueArray;
                keyArray = newKeyArray;
                return null;
            }
        } else {
            throw new NullPointerException("value or key is null");
        }
    }

    /**
     * Rehashes the contents of the hashtable into a hashtable with a larger capacity.<br />
     * This method is called automatically when the number of keys in the hashtable exceeds this hashtable's capacity and load factor.<br />
     * In Smalltable it does nothing, as it constantly has capacity equal to number of keys.
     */
    protected void rehash() {
    }

    /**
     * Removes the key (and its corresponding value) from this hashtable. This method does nothing if the key is not in the hashtable. 
     * @param key the key that needs to be removed. 
     * @return the value to which the key had been mapped in this hashtable, or <b>null</b> if the key did not have a mapping.
     */
    public Object remove(Object key) {
        if (key != null) {
            synchronized (this) {
                int length = valueArray.length;
                for (int j = 0; j < length; j++) {
                    if (keyArray[j].equals(key)) {
                        Object[] newValueArray = new Object[length - 1];
                        System.arraycopy(valueArray, 0, newValueArray, 0, j);
                        System.arraycopy(valueArray, j + 1, newValueArray, j, length - j - 1);
                        Object[] newKeyArray = new Object[length - 1];
                        System.arraycopy(keyArray, 0, newKeyArray, 0, j);
                        System.arraycopy(keyArray, j + 1, newKeyArray, j, length - j - 1);
                        Object previousValue = valueArray[j];
                        valueArray = newValueArray;
                        keyArray = newKeyArray;
                        return previousValue;
                    }
                }
                return null;
            }
        }
        return null;

    }

    /**
     * Returns the number of keys in this hashtable. 
     * @return the number of keys in this hashtable.
     */
    public int size() {
        return valueArray.length;
    }

    /**
     * Returns a rather long string representation of this hashtable. 
     * @return a string representation of this hashtable.
     */
    public String toString() {
        return valueArray.toString();
    }

    /**
     * Returns an enumeration of the values in this hashtable.<br />
     * Use the Enumeration methods on the returned object to fetch the elements sequentially. 
     * @return an enumeration of the values in this hashtable.
     */
    public Enumeration elements() {
        Vector v = new Vector(valueArray.length, 1);
        synchronized (this) {
            for (int i = 0; i < valueArray.length; i++) {
                v.addElement(valueArray[i]);
            }
        }
        return v.elements();
    }

    /**
     * Returns an enumeration of the keys in this hashtable. 
     * @return an enumeration of the keys in this hashtable.
     */
    public Enumeration keys() {
        Vector v = new Vector(keyArray.length, 1);
        synchronized (this) {
            for (int i = 0; i < keyArray.length; i++) {
                v.addElement(keyArray[i]);
            }
        }
        return v.elements();
    }

    public int hashCode() {
        int hash = 0;
        synchronized (this) {
            for (int i = 0; i < keyArray.length; i++) {
                hash += valueArray[i].hashCode() + keyArray[i].hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Smalltable) {
            synchronized (this) {
                if (((Smalltable) obj).keyArray.length == keyArray.length) {
                    for (int i = 0; i < keyArray.length; i++) {
                        if (!(((Smalltable) obj).keyArray[i].equals(keyArray[i])) || !(((Smalltable) obj).valueArray[i].equals(valueArray[i]))) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}

