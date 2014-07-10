package org.paris.batch.exception;

/**
 * 
 * @author tatons
 *
 */
@SuppressWarnings("serial")
public class GeneralMailingException extends GenericBatchException {
	/**
     * 
     */
    public GeneralMailingException() {
        super();
    }

    /**
     * @param message
     */
    public GeneralMailingException(String message) {
        super(message);
    }
}
