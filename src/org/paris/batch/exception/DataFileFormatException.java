package org.paris.batch.exception;

public class DataFileFormatException extends GenericBatchException {
	/** 
	* Cr�e une nouvelle instance de DataFileException
	*/
    public DataFileFormatException() {
        super();
    }

    /**
     * Cr�e une nouvelle instance de DataFileException
     * @param message
     */
    public DataFileFormatException(String message) {
        super(message);
    }
}

