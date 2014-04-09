/**
 * 
 */
package org.paris.batch.exception;

/**
 * @author galloiem
 * 
 */
@SuppressWarnings("serial")
public class RunBatchException extends GenericBatchException {

    /**
     * @param message
     */
    public RunBatchException(String message) {
        super(message);
    }

    /**
     * 
     */
    public RunBatchException() {
        super();
    }

}
