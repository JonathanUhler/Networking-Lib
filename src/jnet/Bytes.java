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
     * Converts an integer to a byte array. This operation will always produce a byte array 
     * {@code b} such that {@code b != null && b.length == 4}.
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
     * Converts a byte array to an integer. The passed array must not be null and must contain 
     * exactly 4 bytes. If the argument array is invalid, {@code Integer.MIN_VALUE} is returned.
     *
     * @param b  the byte array to convert.
     *
     * @return the integer representation of {@code b}.
     *
     * @throws NullPointerException      if {@code b} is null.
     * @throws IllegalArgumentException  if {@code b.length != Integer.SIZE / Byte.SIZE == 4}.
     */
    public static int bytesToInt(byte[] b) {
        if (b == null) {
            throw new NullPointerException("b cannot be null");
        }
        if (b.length != Integer.SIZE / Byte.SIZE) {
            throw new IllegalArgumentException("invalid num bytes: expected 4, found " + b.length);
        }
        
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.order(Bytes.ORDER);
        return buf.getInt();
    }
    
    
    /**
     * Serializes an arbitrary object into a byte array. If the passed {@code object == null} or 
     * an  {@code IOException} is thrown during serialization (e.g. the object is not 
     * {@code Serializable}), then {@code null} is returned.
     *
     * @param object  the object to serialize.
     *
     * @return a byte array representation of {@code object}
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
        }
        catch (IOException e) {
            return null;
        }
        
        return baos.toByteArray();
    }
    
    
    /**
     * Deserializes an arbitrary byte array into an object. If the passed {@code byte[] == null} 
     * or an {@code Exception} is thrown during deserialization, then {@code null} is returned.
     *
     * @param bytes  the byte array to deserialize.
     *
     * @return the deserialized object (it is the responsibility of the caller to cast as 
     *         appropriate).
     */
    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
        catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }
    
    
    /**
     * Converts a string to a byte array. This method is a wrapper for {@code str.getBytes()}. If 
     * {@code str == null}, {@code null} will be returned without an error being logged or thrown.
     *
     * @param str  the string to convert.
     *
     * @return a {@code byte[]} representation of {@code str}.
     */
    public static byte[] stringToBytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes();
    }
    
    
    /**
     * Converts a byte array to a string. This method is a wrapper for {@code new String(bytes)}. 
     * If {@code bytes == null}, {@code null} will be returned without an error being logged or 
     * thrown.
     *
     * @param bytes  the byte array to convert.
     *
     * @return a {@code String} representation of {@code bytes}.
     */
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }
    
}
