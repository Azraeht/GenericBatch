package org.paris.batch.exception;

/**
* Classe d'exception spécifique pour traiter les anomalies de driver pour se connecter
* à la base de données.
* @author Guillaume Weber
* 
*/
@SuppressWarnings("serial")
public class NoPropertiesFoundException extends GenericBatchException {
		
	/**
	* Crée une nouvelle instance de DatabaseDriverNotFoundException
	* @param message Le message détaillant l'exception 
	*/
	public NoPropertiesFoundException(String message)
	{
		super(message);
	}
		
	/**
	* Constructeur par défaut
	*/
	public NoPropertiesFoundException()
	{
		super();
	}

}
