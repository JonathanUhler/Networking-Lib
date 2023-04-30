package jnet;


import java.util.zip.CRC32;
import java.util.Arrays;


/**
 * Utilities to handle CRC32 generation, application, and vertification for network safety.
 * <p>
 * Throughout the methods within this class, the following notation is used to refer to data contained in byte arrays:
 * <ul>
 * <li> {@code header} - a header to allow for safe data I/O. See jnet.Header.
 * <li> {@code payload} - a byte array containing the data being sent or received through the socket. This is
 *                        what is ultimately expected by the programmer and is the only component of the message
 *                        returned by the send/recv methods of the socket-oriented classes in this library.
 * <li> {@code crc} - a 4-byte checksum of {@code payload}. The argument to the method {@code CRC::crc32()} is 
 *                    referred to as {@code bytes} to provide a more generic name, but this method is almost
 *                    exclusively used to generate a checksum for {@code payload}.
 * <li> {@code body} - a byte array containing {@code payload + crc} in that order.
 * <li> {@code message} - the full message sent across the socket, comprised of {@code header + body} in that order.
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
	 * Generates a 4 byte CRC as an integer. This method is a wrapper for the use of 
	 * {@code new java.util.zip.CRC32()}.
	 * <p>
	 * The {@code CRC32::getValue()} routine returns a long which does not necessarily ensure the 4-byte
	 * CRC is within any specific 32 bits of the long. Because of this, truncation is insufficient to isolate
	 * the CRC bytes as an integer, so the mask {@code 0x7FFFFFFF} is used (see 
	 * <a href="https://www.tabnine.com/code/java/methods/java.util.zip.CRC32/getValue">Reference 1</a>).
	 *
	 * @param bytes the bytes to generate a checksum for.
	 *
	 * @return a 4 byte checksum as an int that is assumed to be unsigned.
	 */
	public static int crc32(byte[] bytes) {
		CRC32 crc = new CRC32();
		crc.reset();
		crc.update(bytes);
		return (int) (crc.getValue() & 0x7FFFFFFF); // Shift from long to int, since only 4 bytes are needed
	}


	/**
	 * Generates a 4-byte CRC as a byte array. This method has the same effect as 
	 * {@code jnet.Bytes.intToBytes(CRC.crc32(payload))}. The condition {@code payload == null} is caught
	 * by this method and an error is logged and {@code null} is returned.
	 *
	 * @param payload the payload to generate a crc for.
	 *
	 * @return a little endian byte array whose length is exactly 4 and, when converted to an integer, is
	 *         the crc for {@code payload}.
	 *
	 * @see jnet.Log
	 */
	private static byte[] generate(byte[] payload) {
		if (payload == null || payload.length == 0) {
			Log.stdlog(Log.ERROR, "CRC", "payload too short to generate crc: expected > 0, found " +
					   (payload == null ? "null" : payload.length));
			return null;
		}

		// Get CRC from string
		int crc32 = CRC.crc32(payload);

		// Get CRC as byte array
	    return Bytes.intToBytes(crc32);
	}


	/**
	 * Adds a 4-byte checksum to the end of the argument {@code payload}. If a problem occurs with generating 
	 * the checksum, {@code null} is returned and no error is logged or thrown. {@code payload} must contain 
	 * at least 1 byte, or no checksum will be generated.
	 *
	 * @param payload the byte array to generate a CRC for.
	 *
	 * @return a new byte array containing {@code payload + crc} in that order.
	 */
	public static byte[] attach(byte[] payload) {
		// Get crc as byte array. The generate() call also confirms that payload != null
		byte[] crc = CRC.generate(payload);
		if (crc == null)
			return null;

		// Attach CRC bytes
		byte[] body = new byte[payload.length + crc.length];
		System.arraycopy(payload, 0, body, 0, payload.length);
		System.arraycopy(crc, 0, body, body.length - crc.length, crc.length);
		return body;
	}


	/**
	 * Extracts the payload from a byte array. The argument is assumed to contain a checksum as the last 4 bytes. 
	 * This assumption is validated by {@code CRC::check()}. If the argument {@code body} does not contain at 
	 * least 5 bytes, then null is returned.
	 *
	 * @param body a byte array with a trailing 4-byte crc.
	 *
	 * @return the payload contained within {@code body} as defined by {@code body[0, body.length - 4]}.
	 */
	private static byte[] extractPayload(byte[] body) {
		if (body == null || body.length <= CRC.NUM_BYTES) {
			Log.stdlog(Log.ERROR, "CRC", "body too short to remove crc: expected >" + CRC.NUM_BYTES + ", found " +
					   (body == null ? "null" : body.length));
			return null;
		}

		byte[] payload = new byte[body.length - CRC.NUM_BYTES];
		System.arraycopy(body, 0, payload, 0, payload.length);
		return payload;
	}


	/**
	 * Extracts the CRC from a byte array. The argument is assumed to contain a checksum as the last 4 bytes. 
	 * This assumption is validated by {@code CRC::check()}. If the argument {@code body} does not contain at 
	 * least 5 bytes, then null is returned.
	 *
	 * @param body a byte array with a trailing 4-byte crc.
	 *
	 * @return the CRC of {@code body} as defined by {@code body[body.length - 4, body.length]}.
	 */
	private static byte[] extractCRC(byte[] body) {
	    if (body == null || body.length <= CRC.NUM_BYTES) {
			Log.stdlog(Log.ERROR, "CRC", "body too short to remove payload: expected >" + CRC.NUM_BYTES + ", found " +
					   (body == null ? "null" : body.length));
			return null;
		}

		byte[] crc = new byte[CRC.NUM_BYTES];
		System.arraycopy(body, body.length - CRC.NUM_BYTES, crc, 0, CRC.NUM_BYTES);
		return crc;
	}


	/**
	 * Checks for a valid CRC checksum on a byte array. This method assumes the last 4 bytes of the argument 
	 * represent the CRC valid for the first {@code n - 4} bytes of the array. 
	 * <p>
	 * {@code body} is guaranteed to remain unmodified; only {@code true} or {@code false} is returned if the 
	 * checksum is valid, no bytes are ever changed. Note that {@code false} may be returned upon error. In
	 * this case an error will be logged using jnet.Log.
	 *
	 * @param body the byte array to check the CRC for.
	 *
	 * @return {@code true} if the given and generated CRC of {@code body} match, otherwise {@code false}.
	 *
	 * @see jnet.Log
	 */
	public static boolean check(byte[] body) {
	    if (body == null || body.length <= CRC.NUM_BYTES) {
			Log.stdlog(Log.ERROR, "CRC", "body too short to check: expected >" + CRC.NUM_BYTES + ", found " +
					   (body == null ? "null" : body.length));
			return false;
		}

		// Split up the necessary components
		byte[] payload = CRC.extractPayload(body);
		byte[] crc = CRC.extractCRC(body);
		byte[] gen = CRC.generate(payload);

		// Check for validity
		if (payload == null || crc == null || gen == null) {
			Log.stdlog(Log.ERROR, "CRC", "generated or given crc has invalid length");
			Log.stdlog(Log.ERROR, "CRC", "\tpayload: " + Arrays.toString(payload));
			Log.stdlog(Log.ERROR, "CRC", "\tgiven: " + Arrays.toString(crc));
			Log.stdlog(Log.ERROR, "CRC", "\tgen:   " + Arrays.toString(gen));
			return false;
		}

		// Check the CRC
		boolean passed = Arrays.equals(crc, gen);
		if (!passed) {
			Log.stdlog(Log.ERROR, "CRC", "CRC check failed, given was not equal to generated");
			Log.stdlog(Log.ERROR, "CRC", "\tfull msg: " + Arrays.toString(body));
			Log.stdlog(Log.ERROR, "CRC", "\tgiven crc: " + Arrays.toString(crc));
			Log.stdlog(Log.ERROR, "CRC", "\tgen crc:   " + Arrays.toString(gen));
		}
		return passed;
	}


	/**
	 * Checks a byte array for a valid CRC and returns the payload. If the checksum cannot be validated,
	 * {@code null} is returned.
	 *
	 * @param body the byte array to check the crc for.
	 *
	 * @return the payload of {@code body}.
	 *
	 * @see check
	 */
	public static byte[] checkAndRemove(byte[] body) {
		boolean valid = CRC.check(body);
		if (!valid)
			return null;
		return CRC.extractPayload(body);
	}
	
}
