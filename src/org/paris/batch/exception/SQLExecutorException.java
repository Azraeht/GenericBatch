package org.paris.batch.exception;

/**
 * @author galloiem
 * 
 */
@SuppressWarnings("serial")
public class SQLExecutorException extends GenericBatchException {

    /**
     * 
     */
    public SQLExecutorException() {
        super();
    }

    /**
     * @param message
     */
    public SQLExecutorException(String message) {
        super(message);
    }

}
