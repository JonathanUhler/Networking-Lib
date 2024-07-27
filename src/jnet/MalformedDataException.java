package jnet;


/**
 * A type of {@code RuntimeException} that indicates the presence of bad data.
 *
 * In most contexts, data is determined to be "bad" when the correct number of bytes are present,
 * but their interpretation violates some common principle. This could include a failed CRC check
 * or bytes that represent a negative payload size.
 *
 * @author Jonathan Uhler
 */
public class MalformedDataException extends RuntimeException {

    public MalformedDataException() { }


    public MalformedDataException(String message) {
        super(message);
    }

}
