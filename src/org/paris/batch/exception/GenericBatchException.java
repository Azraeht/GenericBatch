package org.paris.batch.exception;

/**
 * Classe d'exception générique pour le framework GenericBatch.
 * @author webergu
 *
 */
@SuppressWarnings("serial")
public abstract class GenericBatchException extends Exception{
	
	/** 
	* Crée une nouvelle instance de GenericBatchException
	* @param message Le message détaillant l'exception 
	*/
	public GenericBatchException(String message){
		super(message);
	}
	
	/**
	 * Constructeur par défaut
	 */
	public GenericBatchException(){
		super();
	}
	
}
