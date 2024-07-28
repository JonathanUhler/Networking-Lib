package jnet;


import java.util.zip.CRC32;
import java.util.Arrays;


/**
 * Utilities to handle CRC32 generation, application, and vertification for network safety.
 * <p>
 * Throughout the methods within this class, the following notation is used to refer to data 
 * contained in byte arrays:
 * <ul>
 * <li> {@code header} - a header to allow for safe data I/O. See jnet.Header.
 * <li> {@code payload} - a byte array containing the data being sent or received through the 
 *                        socket. This is what is ultimately expected by the programmer and is the 
 *                        only component of the message returned by the send/recv methods of the 
 *                        socket-oriented classes in this library.
 * <li> {@code crc} - a 4-byte checksum of {@code payload}. The argument to the method 
 *                    {@code CRC::crc32()} is referred to as {@code bytes} to provide a more 
 *                    generic name, but this method is almost exclusively used to generate a 
 *                    checksum for {@code payload}.
 * <li> {@code body} - a byte array containing {@code payload + crc} in that order.
 * <li> {@code message} - the full message sent across the socket, comprised of 
 *                        {@code header + body} in that order.
 * </ul>
 *
 * @see jnet.Header
 *
 * @author Jonathan Uhler
 */
public class CRC {
    
    /** The number of nibbles in the checksum. */
    public static final int NUM_NIBBLES = 8;
    /** The number of bytes in the checksum. */
    public static final int NUM_BYTES = CRC.NUM_NIBBLES / 2;
    
    
    private CRC() { }
    
    
    /**
     * Generates a 4 byte CRC as an integer.
     * 
     * This method is a wrapper for the use of {@code new java.util.zip.CRC32()}.
     *
     * @param bytes  the bytes to generate a checksum for.
     *
     * @return a 4 byte checksum as an int that is assumed to be unsigned.
     *
     * @throws NullPointerException  if {@code bytes} is null.
     */
    public static int crc32(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes cannot be null");
        }

        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(bytes);
        return (int) crc.getValue();
    }
    
    
    /**
     * Generates a 4-byte CRC as a byte array.
     *
     * This method has the same effect as {@code jnet.Bytes.intToBytes(CRC.crc32(payload))}.
     * The argument is to this method is expected to be non-null, which must be validated by
     * the caller.
     *
     * @param payload  the payload to generate a crc for.
     *
     * @return a little endian byte array whose length is exactly 4 and, when converted to an 
     *         integer, is the crc for {@code payload}.
     */
    private static byte[] generate(byte[] payload) {
        int crc32 = CRC.crc32(payload);
        return Bytes.intToBytes(crc32);
    }
    
    
    /**
     * Adds a 4-byte checksum to the end of the argument {@code payload} and returns the combined
     * byte array.
     *
     * @param payload  the byte array to generate a CRC for.
     *
     * @return a new byte array containing {@code payload + crc} in that order.
     *
     * @throws NullPointerException  if {@code payload} is null.
     */
    public static byte[] attach(byte[] payload) {
        if (payload == null) {
            throw new NullPointerException("payload cannot be null");
        }

        byte[] crc = CRC.generate(payload);

        byte[] body = new byte[payload.length + crc.length];
        System.arraycopy(payload, 0, body, 0, payload.length);
        System.arraycopy(crc, 0, body, body.length - crc.length, crc.length);
        return body;
    }
    
    
    /**
     * Extracts the payload from a byte array.
     *
     * The argument is assumed to contain a checksum as the last 4 bytes. This assumption is
     * validated by {@code CRC::check()}. The argument is expected to be non-null, which must
     * be validated by the caller.
     *
     * @param body  a byte array with a trailing 4-byte crc.
     *
     * @return the payload contained within {@code body} as defined by 
     *         {@code body[0, body.length - 4]}.
     */
    private static byte[] extractPayload(byte[] body) {
        byte[] payload = new byte[body.length - CRC.NUM_BYTES];
        System.arraycopy(body, 0, payload, 0, payload.length);
        return payload;
    }
    
    
    /**
     * Extracts the CRC from a byte array.
     *
     * The argument is assumed to contain a checksum as the last 4 bytes. This assumption is
     * validated by {@code CRC::check()}. THe argument is expected to be non-null, which must
     * be validated by the caller.
     *
     * @param body  a byte array with a trailing 4-byte crc.
     *
     * @return the CRC of {@code body} as defined by {@code body[body.length - 4, body.length]}.
     */
    private static byte[] extractCRC(byte[] body) {
        byte[] crc = new byte[CRC.NUM_BYTES];
        System.arraycopy(body, body.length - CRC.NUM_BYTES, crc, 0, CRC.NUM_BYTES);
        return crc;
    }
    
    
    /**
     * Checks for a valid CRC checksum on a byte array. This method assumes the last 4 bytes of the
     * argument represent the CRC valid for the first {@code n - 4} bytes of the array. 
     *
     * {@code body} is guaranteed to remain unmodified; only {@code true} or {@code false} is 
     * returned if the checksum is valid, no bytes are ever changed. An exception will be thrown
     * upon encountering any error state or precondition violation.
     *
     * @param body  the byte array to check the CRC for.
     *
     * @return {@code true} if the given and generated CRC of {@code body} match, 
     *         otherwise {@code false}.
     *
     * @throws NullPointerException  if {@code body} is null.
     * @throws MissingDataException  if {@code body} does not contain at least 4 bytes of CRC.
     */
    public static boolean check(byte[] body) {
        if (body == null) {
            throw new NullPointerException("body cannot be null");
        }
        if (body.length < CRC.NUM_BYTES) {
            throw new MissingDataException("byte[" + body.length + "] is too short");
        }

        byte[] payload = CRC.extractPayload(body);
        byte[] crc = CRC.extractCRC(body);
        byte[] gen = CRC.generate(payload);

        return Arrays.equals(crc, gen);
    }
    
    
    /**
     * Checks a byte array for a valid CRC and returns the payload.
     *
     * @param body  the byte array to check the crc for.
     *
     * @return the payload of {@code body}.
     *
     * @throws MalformedDataException  if the CRC is invalid.
     *
     * @see check
     */
    public static byte[] checkAndRemove(byte[] body) {
        boolean valid = CRC.check(body);
        if (!valid) {
            throw new MalformedDataException("crc is not valid");
        }
        return CRC.extractPayload(body);
    }
    
}
