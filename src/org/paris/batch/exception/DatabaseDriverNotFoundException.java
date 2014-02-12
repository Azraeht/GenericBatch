package org.paris.batch.exception;

/**
 * Classe d'exception sp�cifique pour traiter les anomalies de driver pour se connecter
 * � la base de donn�es.
 * @author Guillaume Weber
 *
 */
public class DatabaseDriverNotFoundException extends GenericBatchException {
	
	/**
	* Cr�e une nouvelle instance de DatabaseDriverNotFoundException
	* @param message Le message d�taillant l'exception 
	 */
	public DatabaseDriverNotFoundException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructeur par d�faut
	 */
	public DatabaseDriverNotFoundException()
	{
		super();
	}

}
