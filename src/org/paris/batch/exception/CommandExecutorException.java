package org.paris.batch.exception;

@SuppressWarnings("serial")
public class CommandExecutorException extends GenericBatchException {

    public CommandExecutorException() {
        super();
    }

    public CommandExecutorException(String message) {
        super(message);
    }

}
