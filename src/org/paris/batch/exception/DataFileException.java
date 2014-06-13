package org.paris.batch.exception;
/**
 * @author santusbr
 *
 */
public class DataFileException extends GenericBatchException {

	/** 
	* Crée une nouvelle instance de DataFileException
	*/
    public DataFileException() {
        super();
    }

    /**
     * Crée une nouvelle instance de DataFileException
     * @param message
     */
    public DataFileException(String message) {
        super(message);
    }
}
