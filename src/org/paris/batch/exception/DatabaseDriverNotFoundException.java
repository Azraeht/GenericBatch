package org.paris.batch.exception;

/**
 * Classe d'exception spécifique pour traiter les anomalies de driver pour se connecter
 * à la base de données.
 * @author Guillaume Weber
 *
 */
public class DatabaseDriverNotFoundException extends GenericBatchException {
	
	/**
	* Crée une nouvelle instance de DatabaseDriverNotFoundException
	* @param message Le message détaillant l'exception 
	 */
	public DatabaseDriverNotFoundException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructeur par défaut
	 */
	public DatabaseDriverNotFoundException()
	{
		super();
	}

}
