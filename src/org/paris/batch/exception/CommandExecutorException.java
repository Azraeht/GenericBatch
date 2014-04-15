package org.paris.batch.exception;

/**
 * @author galloiem
 *
 */
@SuppressWarnings("serial")
public class CommandExecutorException extends GenericBatchException {

    /**
     * 
     */
    public CommandExecutorException() {
        super();
    }

    /**
     * @param message
     */
    public CommandExecutorException(String message) {
        super(message);
    }

}
