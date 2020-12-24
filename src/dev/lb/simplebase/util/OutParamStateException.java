package dev.lb.simplebase.util;

import dev.lb.simplebase.util.annotation.Out;

/**
 * Thrown when a method parameter marked with the {@link Out} annotation was in an invalid state.
 * <p>
 * This is a subtype of {@link IllegalStateException}.
 * </p>
 */
public class OutParamStateException extends IllegalStateException {
	private static final long serialVersionUID = 5802713402338046041L;

	/**
	 * Creates a new {@link OutParamStateException} with a message and cause.
	 * @param message The exception message
	 * @param cause The exception that was thrown because an out parameter was in an invalid state
	 */
	public OutParamStateException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new {@link OutParamStateException} with a message.
	 * @param message The exception message
	 */
	public OutParamStateException(String message) {
		super(message);
	}
	
}
