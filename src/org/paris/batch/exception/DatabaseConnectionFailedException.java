package org.paris.batch.exception;

/**
 * Classe d'exception spécifique pour traiter les anomalies de driver pour se
 * connecter à la base de données.
 * 
 * @author webergu
 * 
 */
@SuppressWarnings("serial")
public class DatabaseConnectionFailedException extends GenericBatchException {
    /**
     * Crée une nouvelle instance de DatabaseConnectionFailedException
     * 
     * @param message
     *            Le message détaillant l'exception
     */
    public DatabaseConnectionFailedException(String message) {
        super(message);
    }

    /**
     * Constructeur par défaut
     */
    public DatabaseConnectionFailedException() {
        super();
    }
}
