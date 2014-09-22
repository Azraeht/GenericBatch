package org.paris.batch.exception;

/**
 * 
 * @author Brice SANTUS
 *  *
 */
@SuppressWarnings("serial")
public class CannotFindMessageException extends GenericBatchException {
	/**
     * 
     */
    public CannotFindMessageException() {
        super();
    }

    /**
     * @param message
     */
    public CannotFindMessageException(String message) {
        super(message);
    }
}
