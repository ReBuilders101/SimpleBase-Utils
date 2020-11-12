package dev.lb.simplebase.util.task;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import dev.lb.simplebase.util.annotation.Threadsafe;
import dev.lb.simplebase.util.task.Task.State;

/**
 * Provides methods to complete or fail a blocking task.
 */
@Threadsafe
public final class TaskCompleter {

	private final TaskCompleterOf<Void> inner;
	
	/**
	 * Creates a {@link TaskCompleter} that is not associated with any task
	 */
	public TaskCompleter() {
		this.inner = new TaskCompleterOf<>();
	}
	
	/**
	 * Attempts to complete the task with a {@link State#SUCCESS} state. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @return {@code true} if the task has switched to {@link State#SUCCESS}, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalSuccess() {
		return inner.signalSuccess(null);
	}
	
	/**
	 * Attempts to complete the task with a {@link State#FAILED} state and an exception. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param throwable The {@link Throwable} that caused the task to fail
	 * @return {@code true} if the task has switched to {@link State#FAILED}, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalFailure(Throwable throwable) {
		return inner.signalFailure(throwable);
	}
	
	void setup(/*notnull*/BooleanSupplier success, /*notnull*/Predicate<Throwable> fail) {
		inner.setup((t) -> success.getAsBoolean(), fail);
	}
	
	TaskCompleterOf<Void> inner() {
		return inner;
	}
	
	/**
	 * Creates a {@link TaskCompleter} that is not associated with any task
	 * @return A new {@link TaskCompleter}
	 */
	public static TaskCompleter create() {
		return new TaskCompleter();
	}
}
