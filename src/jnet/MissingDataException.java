package jnet;


/**
 * A type of {@code RuntimeException} that identifies a piece of data that was shorter or longer
 * than expected and cannot be processed because of the discrepancy in length.
 *
 * @author Jonathan Uhler
 */
public class MissingDataException extends RuntimeException {

    public MissingDataException() { }


    public MissingDataException(String message) {
        super(message);
    }

}
