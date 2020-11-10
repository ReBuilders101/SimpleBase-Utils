package dev.lb.simplebase.util.task;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * A {@link TaskFailureRequestException} can be thrown when a task is required to fail
 * without a specific cause exception.
 * <p>
 * Can be thrown with {@link Task#fail(String)}.
 * </p>
 */
public class TaskFailureRequestException extends RuntimeException {
	private static final long serialVersionUID = 9065735530350527081L;

	@Internal
	TaskFailureRequestException(String message) {
		super(message);
	}

}
