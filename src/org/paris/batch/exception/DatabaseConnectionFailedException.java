package org.paris.batch.exception;

/**
 * Classe d'exception sp�cifique pour traiter les anomalies de driver pour se
 * connecter � la base de donn�es.
 * 
 * @author webergu
 * 
 */
@SuppressWarnings("serial")
public class DatabaseConnectionFailedException extends GenericBatchException {
    /**
     * Cr�e une nouvelle instance de DatabaseConnectionFailedException
     * 
     * @param message
     *            Le message d�taillant l'exception
     */
    public DatabaseConnectionFailedException(String message) {
        super(message);
    }

    /**
     * Constructeur par d�faut
     */
    public DatabaseConnectionFailedException() {
        super();
    }
}
