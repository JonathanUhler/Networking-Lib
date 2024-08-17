package jnet;


/**
 * Utilities to handle header generation, application, and verification for socket buffer safety. 
 * The inclusion of fixed-size headers with CRC redundancy allows a specific payload length to be 
 * read, assuming the integrity of the header. Upon header corruption, other tactics (e.g. 
 * sequence numbers and resends) can be used to verify data transmission.
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
 * <p>
 * The header managed by this class is defined as a byte array containing the following:
 * <table style="border: 1px solid black">
 *  <caption>Bytes</caption>
 *  <tr style="border: 1px solid black">
 *   <th style="border: 1px solid black"> Byte
 *   <th style="border: 1px solid black"> Commentary
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> b[0]
 *   <td style="border: 1px solid black"> Header identifier, always 0x68 ("h").
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> b[1]
 *   <td style="border: 1px solid black"> Reserved (sequence number).
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> b[3, 2]
 *   <td style="border: 1px solid black"> unused
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> b[7, 4]
 *   <td style="border: 1px solid black"> Body length. Defined as 
 *                                        {@code (payload + crc).length == body.length}.
 *  </tr>
 *  <tr style="border: 1px solid black">
 *   <td style="border: 1px solid black"> b[11, 8]
 *   <td style="border: 1px solid black"> Header CRC. This is a checksum only for {@code b[0, 7]} 
 *                                        in the header. The body is assumed to contain its own 
 *                                        checksum if needed.
 *  </tr>
 * </table>
 *
 * @see jnet.CRC
 *
 * @author Jonathan Uhler
 */
public class Header {
    
    /** The size, in bytes, of the header. */
    public static final int SIZE = 8 + CRC.NUM_BYTES;
    /** b[0] of the header. */
    public static final byte HEADER_BYTE = 0x68;
    /** The offset, in bytes, of the body length portion of the header (b[7, 4]). */
    public static final int BODY_LENGTH_OFFSET = 4;
    /** The length, in bytes, of the body length portion of the header (b[7, 4]). */
    public static final int BODY_LENGTH_SIZE = 4;
    /** The offset, in bytes, of the crc portion of the header (b[11, 8]). */
    public static final int CRC_OFFSET = 8;
    /** The length, in bytes, of the crc portion of the header (b[11, 8]). */
    public static final int CRC_SIZE = CRC.NUM_BYTES;
    
    
    private Header() { }
    
    
    /**
     * Generates a header for a given body.
     *
     * The CRC bytes attached to {@code body} WILL be validated by this method. If the CRC is
     * found to be invalid, an exception is thrown.
     *
     * @param body  a payload and crc to generate a header for
     *
     * @return a byte array containing the header for {@code body}.
     * 
     * @throws MalformedDataException  if the body has an invalid CRC.
     */
    private static byte[] generate(byte[] body) {
        boolean hasValidCRC = CRC.check(body);
        if (!hasValidCRC) {
            throw new MalformedDataException("body has invalid crc");
        }

        int length = body.length;
        byte[] lengthBytes = Bytes.intToBytes(length);

        byte[] header = new byte[Header.SIZE - CRC.NUM_BYTES];
        header[0] = Header.HEADER_BYTE;
        System.arraycopy(lengthBytes, 0, header,
                         Header.BODY_LENGTH_OFFSET,
                         Header.BODY_LENGTH_SIZE);
        header = CRC.attach(header);
        return header;
    }
    
    
    /**
     * Creates and attaches a header to the argument {@code body}.
     *
     * A new byte array is returned containing {@code header + body} in that order. The contents
     * of {@code body} are not modified.
     *
     * @param body  the message body (which must contain a crc) to generate and attach a header to.
     *
     * @return a new array containing {@code header + body} in that order.
     *
     * @throws NullPointerException    if {@code body} is null.
     * @throws MalformedDataException  if the body has an invalid CRC.
     */
    public static byte[] attach(byte[] body) {
        if (body == null) {
            throw new NullPointerException("body cannot be null");
        }

        byte[] header = Header.generate(body);
        byte[] message = new byte[header.length + body.length];
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(body, 0, message, message.length - body.length, body.length);
        return message;
    }
    
    
    /**
     * Validates and parses the content of a given header as a {@code Header.Info} object.
     *
     * "Validation" includes a check of the header crc. Upon any validation error, an
     * exception is thrown. If the validation succeeds, the data in the struct is guaranteed to
     * be valid.
     *
     * @param header  the header to validate.
     *
     * @return a {@code Header.Info} struct containing all the information of the header in more
     *         accessible public instance variables.
     *
     * @see jnet.Header.Info
     */
    public static Info validateAndParse(byte[] header) {
        return new Info(header);
    }
    
    
    
    /**
     * Wrapper structure for the byte-array representation of a header. The constructor of this 
     * class automatically validates the header byte-array for correctness.
     *
     * @author Jonathan Uhler
     */
    public static class Info {
        
        /** The header id (b[0]). */
        public byte id;
        /** The size of the body attached to this header (b[7, 4]). */
        public int size;
        /** The crc checksum for the header only (b[11, 8]). */
        public int crc;
        
        
        /**
         * Constructs a {@code Header.Info} object from a header byte array. This constructor 
         * validates the information contained within the argument {@code header}. If the data can 
         * be validated the instance variables of this class are set appropriately. Upon error an 
         * exception is thrown.
         *
         * @param header  the header to validate and parse.
         *
         * @throws NullPointerException    if {@code header} is null.
         * @throws MissingDataException    if the header length is not {@code Header.SIZE}.
         * @throws MalformedDataException  if the CRC in the header is invalid.
         * @throws MalformedDataException  if the payload size in the header negative.
         */
        public Info(byte[] header) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            }
            if (header.length != Header.SIZE) {
                throw new MissingDataException("invalid header length, expected " +
                                               Header.SIZE + ", found " + header.length);
            }
            if (!CRC.check(header)) {
                throw new MalformedDataException("invalid header crc in " + Bytes.toString(header));
            }
            
            byte[] size = new byte[Header.BODY_LENGTH_SIZE];
            System.arraycopy(header, Header.BODY_LENGTH_OFFSET, size, 0, size.length);
            byte[] crc = new byte[Header.CRC_SIZE];
            System.arraycopy(header, Header.CRC_OFFSET, crc, 0, crc.length);
            
            this.id = header[0];
            this.size = Bytes.bytesToInt(size);
            this.crc = Bytes.bytesToInt(crc);
            
            if (this.size < 0) {
                throw new MalformedDataException("invalid payload size: " + this.size);
            }
        }
	
    }
    
}
