package org.paris.batch.exception;

/**
 * 
 * @author tatons
 *
 */
@SuppressWarnings("serial")
public class CannotWriteTextToMessageException extends GenericBatchException {
	/**
     * 
     */
    public CannotWriteTextToMessageException() {
        super();
    }

    /**
     * @param message
     */
    public CannotWriteTextToMessageException(String message) {
        super(message);
    }
}
