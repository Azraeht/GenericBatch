package org.paris.batch.exception;

/**
 * 
 * @author Brice SANTUS
 *  *
 */
@SuppressWarnings("serial")
public class CannotJoinAttachementException extends GenericBatchException {
	/**
     * 
     */
    public CannotJoinAttachementException() {
        super();
    }

    /**
     * @param message
     */
    public CannotJoinAttachementException(String message) {
        super(message);
    }
}
