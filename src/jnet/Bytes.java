package jnet;


import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;


/**
 * Utilities to handle byte-string and byte-object operations.
 *
 * @author Jonathan Uhler
 */
public class Bytes {
    
    /** The expected order of all bytes used by this class. */
    public static final ByteOrder ORDER = ByteOrder.LITTLE_ENDIAN;
    
    
    private Bytes() { }
    
    
    /**
     * Converts an integer to a byte array.
     *
     * This operation will always produce a non-null byte array with exactly four elements.
     * The returned byte array is always little endian.
     *
     * @param n  the integer to convert.
     *
     * @return a byte array representation of {@code n}.
     */
    public static byte[] intToBytes(int n) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        buf.order(Bytes.ORDER);
        buf.putInt(n);
        return buf.array();
    }
    
    
    /**
     * Converts a byte array to an integer.
     *
     * @param b  the byte array to convert.
     *
     * @return the integer representation of {@code b}.
     *
     * @throws NullPointerException       the {@code b} is null.
     * @throws jnet.MissingDataException  if the {@code b} does not contain exactly four bytes.
     */
    public static int bytesToInt(byte[] b) {
        if (b == null) {
            throw new NullPointerException("b cannot be null");
        }
        if (b.length != Integer.SIZE / Byte.SIZE) {
            throw new MissingDataException("cannot convert byte[" + b.length + "] to int");
        }
        
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.order(Bytes.ORDER);
        return buf.getInt();
    }
    
    
    /**
     * Serializes an arbitrary object into a byte array.
     *
     * @param object  the object to serialize.
     *
     * @return a byte array representation of {@code object}
     *
     * @throws NullPointerException    if {@code object} is null.
     * @throws SerializationException  if an IO error occurs during serialization.
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            throw new NullPointerException("object cannot be null");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
        }
        catch (IOException e) {
            throw new SerializationException("cannot serialize: " + e);
        }
        
        return baos.toByteArray();
    }
    
    
    /**
     * Deserializes an arbitrary byte array into an object.
     *
     * @param bytes  the byte array to deserialize.
     *
     * @return the deserialized object (it is the responsibility of the caller to cast as 
     *         appropriate).
     *
     * @throws NullPointerException    if {@code bytes} is null.
     * @throws SerializationException  if an IO error occurs during deserialization.
     */
    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes cannot be null");
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
        catch (ClassNotFoundException | IOException e) {
            throw new SerializationException("cannot deserialize: " + e);
        }
    }
    
    
    /**
     * Converts a string to a byte array.
     *
     * This method is a wrapper for {@code str.getBytes()}.
     *
     * @param str  the string to convert.
     *
     * @return a {@code byte[]} representation of {@code str}.
     *
     * @throws NullPointerException  if {@code str} is null.
     */
    public static byte[] stringToBytes(String str) {
        if (str == null) {
            throw new NullPointerException("str cannot be null");
        }
        return str.getBytes();
    }
    
    
    /**
     * Converts a byte array to a string.
     *
     * This method is a wrapper for {@code new String(bytes)}.
     *
     * @param bytes  the byte array to convert.
     *
     * @return a {@code String} representation of {@code bytes}.
     *
     * @throws NullPointerException  if {@code bytes} is null.
     */
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes cannot be null");
        }
        return new String(bytes);
    }


    /**
     * Returns a string array representation of a byte array.
     *
     * @param bytes  the byte array to convert to a string.
     */
    public static String toString(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        for (byte b : bytes) {
            str.append(String.format("%02X", b));
        }
        return str.toString();
    }
    
}
