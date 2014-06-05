package org.paris.batch.exception;

public class DataFileFormatException extends GenericBatchException {
	/** 
	* Crée une nouvelle instance de DataFileFormatException
	*/
    public DataFileFormatException() {
        super();
    }

    /**
     * Crée une nouvelle instance de DataFileFormatException
     * @param message
     */
    public DataFileFormatException(String message) {
        super(message);
    }
}

