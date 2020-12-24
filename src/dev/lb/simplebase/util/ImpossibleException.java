package dev.lb.simplebase.util;

/**
 * If this exception appears is thrown anywhere, it means that the code is broken. Should never appear.
 * Should also not be caught because objects might be in an inconsistent state after this apperas.
 */
public class ImpossibleException extends RuntimeException {
	private static final long serialVersionUID = 4288953546780824163L;
	
	/**
	 * Creates a new instance of {@link ImpossibleException}.
	 * @param message Since the exceptions type name is not very descriptive, this message should be
	 */
	public ImpossibleException(String message) {
		super(message);
	}
}
