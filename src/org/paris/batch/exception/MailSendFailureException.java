package org.paris.batch.exception;

/**
 * 
 * @author tatons
 *
 */
@SuppressWarnings("serial")
public class MailSendFailureException extends GenericBatchException {
	/**
     * 
     */
    public MailSendFailureException() {
        super();
    }

    /**
     * @param message
     */
    public MailSendFailureException(String message) {
        super(message);
    }
}
