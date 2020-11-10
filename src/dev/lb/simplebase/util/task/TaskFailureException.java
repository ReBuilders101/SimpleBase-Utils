package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutionException;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * An unchecked exception that wraps both checked and unchecked exceptions produced when executing a
 * {@link Task}. Equivalent to {@link ExecutionException}.
 */
public class TaskFailureException extends RuntimeException {
	private static final long serialVersionUID = -636967615249726625L;

	/**
	 * Creates a new {@link TaskFailureException} with a cause
	 * @param primaryCause The cause of the failure
	 */
	@Internal
	TaskFailureException(Throwable primaryCause) {
		super("Task failed with an exception", primaryCause);
	}

}
