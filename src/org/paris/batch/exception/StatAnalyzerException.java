package org.paris.batch.exception;

/**
 * 
 * @author paramesm
 *
 */
@SuppressWarnings("serial")
public class StatAnalyzerException extends GenericBatchException {

	public StatAnalyzerException() {
		super();
	}

	/**
	 * @param message
	 */
	public StatAnalyzerException(String message) {
		super(message);
	}
}
