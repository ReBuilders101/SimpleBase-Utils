package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * A {@link CancelCondition} object can be used to request cancellation of an asynchronous or blocking task.
 * Cancellation may not be supported by all types of task.
 */
public interface CancelCondition {

	/**
	 * Attempts to cancel the associated task or blocking action.
	 * @return {@code true} if cancellation was successful, {@code false} if not
	 */
	public default boolean cancel() {
		return cancel(null);
	}
	
	/**
	 * Attempts to cancel the associated task or blocking action.
	 * @param exceptionPayload A nullable object that will be available on the {@link TaskCancellationException} caused by cancelling the task
	 * @return {@code true} if cancellation was successful, {@code false} if not
	 */
	public boolean cancel(/*Nullable*/ Object exceptionPayload);
	
	/**
	 * Returns {@code true} when cancelling the associated action is no longer possible because the action was completed,
	 * has failed or was already cancelled.
	 * @return {@code true} if the action can no longer be cancelled, {@code false} if cancellation is still possible
	 */
	public boolean isCancellationExpired();
	
	/**
	 * Adds a handler that will be run when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet cancelled, the action will run
	 * on the thread that calls the {@link #cancel()} method on this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public CancelCondition onCancelled(Consumer<TaskCancellationException> action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public default CancelCondition onCancelledAsync(Consumer<TaskCancellationException> action) {
		return onCancelledAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	public CancelCondition onCancelledAsync(Consumer<TaskCancellationException> action, ExecutorService executor);
	
	/**
	 * Creates a standalone {@link CancelCondition} not directly associated with any task or action.
	 * <p>
	 * The created condition can be associated with one or more blocking operations by passing it as
	 * a parameter, e.g. {@link Task#await(CancelCondition)} will associate the {@code CancelCondition}
	 * with the blocking {@code await} call (and not with the awaited task).
	 * </p><p>
	 * A {@link Task} extends {@link CancelCondition}, so it is not necessary to create a separate condition to cancel a {@code Task}.
	 * </p>
	 * @return A new {@link CancelCondition} not assicated with any action
	 */
	public static CancelCondition create() {
		return new SubscriptionHandler.StandaloneCancelCondition();
	}
}