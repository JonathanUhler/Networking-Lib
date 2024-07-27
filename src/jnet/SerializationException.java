package jnet;


/**
 * A type of {@code RuntimeException} that occurs if an object fails to be serialized or
 * deserialized to or from a byte array.
 *
 * @author Jonathan Uhler
 */
public class SerializationException extends RuntimeException {

    public SerializationException() { }


    public SerializationException(String message) {
        super(message);
    }

}
