package org.paris.batch.exception;

@SuppressWarnings("serial")
public class SQLExecutorException extends GenericBatchException {

    public SQLExecutorException() {
        super();
    }

    public SQLExecutorException(String message) {
        super(message);
    }

}
