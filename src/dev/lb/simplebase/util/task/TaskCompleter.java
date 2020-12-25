package dev.lb.simplebase.util.task;

import java.util.function.Supplier;

import dev.lb.simplebase.util.annotation.Threadsafe;
import dev.lb.simplebase.util.function.PredicateEx;
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
	 * @throws CancelledException When the task was already cancelled 
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalSuccess() throws CancelledException {
		return inner.signalSuccess(null);
	}
	
	/**
	 * Attempts to complete the task with a {@link State#FAILED} state and an exception. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param throwable The {@link Throwable} that caused the task to fail
	 * @return {@code true} if the task has switched to {@link State#FAILED}, {@code false} if not
	 * @throws CancelledException When the task was already cancelled 
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalFailure(Throwable throwable) throws CancelledException {
		return inner.signalFailure(throwable);
	}
	
	void setup(/*notnull*/PredicateEx<Void, CancelledException> success, /*notnull*/PredicateEx<Throwable, CancelledException> fail,
			Supplier<CancelledException> ex) {
		inner.setup(success, fail, ex);
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
	
	/**
	 * Can be used to check whether the task that this {@link TaskCompleter} will complete has been cancelled
	 * (by calling {@link Task#cancel()} on the task). If it is cancelled, attempting to signal success/failure
	 * will result in a {@link CancelledException}. The exception object can be retrieved with {@link #getCancellationException()}.
	 * @return {@code true} if the associated Task was cancelled, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean isCancelled() {
		return inner.isCancelled();
	}
	
	/**
	 * The {@link CancelledException} that was created by cancelling the associated {@link Task},
	 * or {@code null} if the task was not cancelled.
	 * @return The {@link CancelledException} that cancelled the associated task, if present
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public CancelledException getCancellationException() {
		return inner.getCancellationException();
	}
}
