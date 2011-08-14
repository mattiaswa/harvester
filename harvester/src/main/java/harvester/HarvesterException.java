/**
 * 
 */
package harvester;

/**
 * @author mattias
 * 
 */
public class HarvesterException extends RuntimeException {

	public HarvesterException(String message, Throwable cause) {
		super(message, cause);
	}

	public HarvesterException(Exception cause) {
		super(cause);
	}

	private static final long serialVersionUID = 6452122986047152340L;

}
