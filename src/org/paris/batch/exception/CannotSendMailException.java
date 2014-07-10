package org.paris.batch.exception;

/**
 * 
 * @author tatons
 *
 */
@SuppressWarnings("serial")
public class CannotSendMailException extends GenericBatchException {
	/**
     * 
     */
    public CannotSendMailException() {
        super();
    }

    /**
     * @param message
     */
    public CannotSendMailException(String message) {
        super(message);
    }
}
