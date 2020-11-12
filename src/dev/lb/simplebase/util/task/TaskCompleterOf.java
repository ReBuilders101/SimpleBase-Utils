package dev.lb.simplebase.util.task;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import dev.lb.simplebase.util.task.Task.State;

/**
 * Provides methods to complete or fail a blocking task.
 * @param <T> The result type of the {@link TaskOf}
 */
public class TaskCompleterOf<T> {

	private volatile Predicate<T> successComplete;
	private volatile Predicate<Throwable> failComplete;
	
	private final AtomicInteger state;
	private static final int UNSET = 0;
	private static final int SETTING = 1;
	private static final int SET = 2;
	
	/**
	 * Creates a {@link TaskCompleter} that is not associated with any task
	 */
	public TaskCompleterOf() {
		this.state = new AtomicInteger(UNSET);
		this.successComplete = null;
		this.failComplete = null;
	}
	
	/**
	 * Attempts to complete the task with a {@link State#SUCCESS} state. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param returnValue The result for the {@link TaskOf} that should be completed
	 * @return {@code true} if the task has switched to {@link State#SUCCESS}, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalSuccess(T returnValue) {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return successComplete.test(returnValue);
		}
	}
	
	/**
	 * Attempts to complete the task with a {@link State#FAILED} state and an exception. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param throwable The {@link Throwable} that caused the task to fail
	 * @return {@code true} if the task has switched to {@link State#FAILED}, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalFailure(Throwable throwable) {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return failComplete.test(throwable);
		}
	}
	
	void setup(/*notnull*/Predicate<T> success, /*notnull*/Predicate<Throwable> fail) {
		if(!state.compareAndSet(UNSET, SETTING)) {
			throw new IllegalArgumentException("TaskCompletionSource is already in use"); //Yes, Illegal arg not state
		}
		
		this.successComplete = success;
		this.failComplete = fail;
		
		if(!state.compareAndSet(SETTING, SET)) {
			throw new ConcurrentModificationException("TaskCompletionSource SETTING state modified by concurrent thread");
		}
	}
	
	/**
	 * Creates a {@link TaskCompleter} that is not associated with any task
	 * @param <T> The result type of the {@link TaskOf} that will be used
	 * @return A new {@link TaskCompleter}
	 */
	public static <T> TaskCompleterOf<T> create() {
		return new TaskCompleterOf<>();
	}
}
