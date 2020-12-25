package dev.lb.simplebase.util.task;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import dev.lb.simplebase.util.ImpossibleException;
import dev.lb.simplebase.util.function.IntPredicateEx;
import dev.lb.simplebase.util.function.PredicateEx;
import dev.lb.simplebase.util.task.Task.State;

/**
 * Provides methods to complete or fail a blocking task.
 */
public class TaskCompleterOfInt {

	private volatile IntPredicateEx<CancelledException> successComplete;
	private volatile PredicateEx<Throwable, CancelledException> failComplete;
	private volatile Supplier<CancelledException> cancelEx;
	
	private final AtomicInteger state;
	private static final int UNSET = 0;
	private static final int SETTING = 1;
	private static final int SET = 2;
	
	/**
	 * Creates a {@link TaskCompleterOfInt} that is not associated with any task
	 */
	public TaskCompleterOfInt() {
		this.state = new AtomicInteger(UNSET);
		this.successComplete = null;
		this.failComplete = null;
		this.cancelEx = null;
	}
	
	/**
	 * Attempts to complete the task with a {@link State#SUCCESS} state. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param returnValue The result for the {@link TaskOf} that should be completed
	 * @return {@code true} if the task has switched to {@link State#SUCCESS}, {@code false} if not
	 * @throws CancelledException When the task was already cancelled
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalSuccess(int returnValue) throws CancelledException {
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
	 * @throws CancelledException When the task was already cancelled
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean signalFailure(Throwable throwable) throws CancelledException {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return failComplete.test(throwable);
		}
	}
	
	void setup(/*notnull*/IntPredicateEx<CancelledException> success, /*notnull*/PredicateEx<Throwable, CancelledException> fail, Supplier<CancelledException> ex) {
		if(!state.compareAndSet(UNSET, SETTING)) {
			throw new IllegalArgumentException("TaskCompletionSource is already in use"); //Yes, Illegal arg not state
		}
		
		this.successComplete = success;
		this.failComplete = fail;
		this.cancelEx = ex;
		
		if(!state.compareAndSet(SETTING, SET)) {
			throw new ImpossibleException("TaskCompletionSource SETTING state modified by concurrent thread");
		}
	}
	
	/**
	 * Can be used to check whether the task that this {@link TaskCompleterOfBool} will complete has been cancelled
	 * (by calling {@link TaskOfBool#cancel()} on the task). If it is cancelled, attempting to signal success/failure
	 * will result in a {@link CancelledException}. The exception object can be retrieved with {@link #getCancellationException()}.
	 * @return {@code true} if the associated Task was cancelled, {@code false} if not
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public boolean isCancelled() {
		return getCancellationException() != null;
	}
	
	/**
	 * The {@link CancelledException} that was created by cancelling the associated {@link TaskOfBool},
	 * or {@code null} if the task was not cancelled.
	 * @return The {@link CancelledException} that cancelled the associated task, if present
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public CancelledException getCancellationException() {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return cancelEx.get();
		}
	}
	
	/**
	 * Creates a {@link TaskCompleter} that is not associated with any task
	 * @return A new {@link TaskCompleter}
	 */
	public static TaskCompleterOfInt create() {
		return new TaskCompleterOfInt();
	}

	/**
	 * Attempts to complete the task with a {@link State#SUCCESS} state without any indication whether
	 * thsi attempt succeeded. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param value The result for the {@link TaskOf} that should be completed
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public void trySignalSuccess(int value) {
		try {
			signalSuccess(value);
		} catch (CancelledException e) {}
	}
	
	/**
	 * Attempts to complete the task with a {@link State#FAILED} state without any indication whether
	 * thsi attempt succeeded. This can fail when the associated
	 * task is already completed or is being completed by another thread.
	 * @param throwable The {@link Throwable} that caused the task to fail
	 * @throws IllegalStateException When the completer is not associated with any task
	 */
	public void trySignalFailure(Throwable throwable) {
		try {
			signalFailure(throwable);
		} catch (CancelledException e) {}
	}
}
