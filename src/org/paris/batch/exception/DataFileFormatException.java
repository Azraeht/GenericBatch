package org.paris.batch.exception;

public class DataFileFormatException extends GenericBatchException {
	/** 
	* Crée une nouvelle instance de DataFileException
	*/
    public DataFileFormatException() {
        super();
    }

    /**
     * Crée une nouvelle instance de DataFileException
     * @param message
     */
    public DataFileFormatException(String message) {
        super(message);
    }
}

