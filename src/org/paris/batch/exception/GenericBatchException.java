package org.paris.batch.exception;

/**
 * Classe d'exception g�n�rique pour le framework GenericBatch.
 * @author webergu
 *
 */
@SuppressWarnings("serial")
public abstract class GenericBatchException extends Exception{
	
	/** 
	* Cr�e une nouvelle instance de GenericBatchException
	* @param message Le message d�taillant l'exception 
	*/
	public GenericBatchException(String message){
		super(message);
	}
	
	/**
	 * Constructeur par d�faut
	 */
	public GenericBatchException(){
		super();
	}
	
}
