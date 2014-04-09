package org.paris.batch.exception;

/**
* Classe d'exception sp�cifique pour traiter les anomalies de driver pour se connecter
* � la base de donn�es.
* @author Guillaume Weber
* 
*/
@SuppressWarnings("serial")
public class NoPropertiesFoundException extends GenericBatchException {
		
	/**
	* Cr�e une nouvelle instance de DatabaseDriverNotFoundException
	* @param message Le message d�taillant l'exception 
	*/
	public NoPropertiesFoundException(String message)
	{
		super(message);
	}
		
	/**
	* Constructeur par d�faut
	*/
	public NoPropertiesFoundException()
	{
		super();
	}

}
