package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Out;
import dev.lb.simplebase.util.function.BooleanConsumer;
import dev.lb.simplebase.util.value.OptionalBoolean;

/**
 * A {@link Task} that contains a boolean result.
 */
public interface TaskOfBool extends Task {
	
	/**
	 * Adds a handler that will be run when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet done, the action will run
	 * on the thread that causes this task to be completed successfully.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public TaskOfBool onSuccess(BooleanConsumer action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public default TaskOfBool onSuccessAsync(BooleanConsumer action) {
		return onSuccessAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task completes successfully
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	public TaskOfBool onSuccessAsync(BooleanConsumer action, ExecutorService executor);
	
	/**
	 * Waits for this task to be completed.
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 */
	@Override public TaskOfBool await() throws InterruptedException;
	/**
	 * Waits for this task to be completed. Ignores interruption of the waiting thread.
	 * @return This task
	 */
	@Override public TaskOfBool awaitUninterruptibly();
	/**
	 * Waits for this task to be completed or until the timeout elapses.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	@Override public TaskOfBool await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	/**
	 * Waits for this task to be completed or until the timeout elapses. Ignores interruption of the waiting thread.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return This task
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	@Override public TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException;
	/**
	 * Waits for this task to be completed or until the condition is cancelled.
	 * @param condition <i>&#064;Out</i> The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws OutParamStateException When the {@code condition} has already been assoicated with a different action
	 * @throws NullPointerException When {@code condition} is {@code null}
	 */
	@Override public TaskOfBool await(@Out CancelCondition condition) throws InterruptedException, CancelledException, OutParamStateException;
	/**
	 * Waits for this task to be completed or until the condition is cancelled. Ignores interruption of the waiting thread.
	 * @param condition <i>&#064;Out</i> The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws OutParamStateException When the {@code condition} has already been assoicated with a different action
	 * @throws NullPointerException When {@code condition} is {@code null}
	 */
	@Override public TaskOfBool awaitUninterruptibly(@Out CancelCondition condition) throws CancelledException, OutParamStateException;
	/**
	 * Waits for this task to be completed or until the timeout elapses or until the condition is cancelled.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param condition <i>&#064;Out</i> The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws OutParamStateException When the {@code condition} has already been assoicated with a different action
	 * @throws NullPointerException When {@code unit} or {@code condition} is {@code null}
	 */
	@Override public TaskOfBool await(long timeout, TimeUnit unit, @Out CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException, OutParamStateException;
	/**
	 * Waits for this task to be completed or until the timeout elapses or until the condition is cancelled. Ignores interruption of the waiting thread.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param condition <i>&#064;Out</i> The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws OutParamStateException When the {@code condition} has already been assoicated with a different action
	 * @throws NullPointerException When {@code unit} or {@code condition} is {@code null}
	 */
	@Override public TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit, @Out CancelCondition condition) throws TimeoutException, CancelledException, OutParamStateException;
	/**
	 * Checks for any exceptions thrown by the action associated with this task, and rethrows that exception.
	 * <p>
	 * This action will consume the exception: When calling this method twice in succession, the second call will never throw an
	 * exception even if the first one did. This ensures that the same exception is only thrown and handled once.
	 * </p>
	 * @return This task
	 * @throws Throwable The {@link Throwable} that caused the task to fail, if present
	 * @see #hasUnconsumedException()
	 */
	@Override public TaskOfBool checkFailure() throws Throwable;
	/**
	 * Checks for any exceptions of type {@code E} thrown by the action associated with this task, and rethrows that exception.
	 * <p>
	 * This action will consume the exception: When calling this method twice in succession, the second call will never throw an
	 * exception even if the first one did. This ensures that the same exception is only thrown and handled once.
	 * </p><p>
	 * If the task has an exception that caused it to fail that is not of type {@code E}, then this method returns
	 * normally and the exception will not be consumed.
	 * </p>
	 * @param <E> The type of {@code Throwable} to check for
	 * @param expectedType The excpecetd type of the {@code Throwable}
	 * @return This task
	 * @throws E The {@link Throwable} of type {@code E} that caused the task to fail, if present
	 * @throws NullPointerException When {@code expectedType} is {@code null}
	 */
	@Override public <E extends Throwable> TaskOfBool checkFailure(Class<E> expectedType) throws E;
	/**
	 * Checks for any exceptions thrown by the action associated with this task,
	 * or for cancellation of this task.
	 * If one is present, it is wrapped in an unchecked {@link TaskFailureException} and thrown.
	 * <p>
	 * This action will consume the exception: When calling this method twice in succession, the second call will never throw an
	 * exception even if the first one did. This ensures that the same exception is only thrown and handled once.
	 * </p>
	 * @return This task
	 * @throws TaskFailureException The {@link TaskFailureException} that wraps the cause of failure, if present
	 * @throws CancelledException When the task was cancelled
	 */
	@Override public TaskOfBool checkSuccess() throws TaskFailureException, CancelledException;
	/**
	 * Adds a handler that will be run when this task is cancelled.
	 * <p>
	 * If the task is already cancelled when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet cancelled, the action will run
	 * on the thread that calls the {@link #cancel()} method on this task.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is cancelled
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public TaskOfBool onCancelled(Consumer<CancelledException> action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task is cancelled.
	 * <p>
	 * If the task is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this task.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is cancelled
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public default TaskOfBool onCancelledAsync(Consumer<CancelledException> action) {
		return onCancelledAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this task is cancelled.
	 * <p>
	 * If the task is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this task.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is cancelled
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	@Override public TaskOfBool onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor);
	/**
	 * Adds a handler that will be run when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet done, the action will run
	 * on the thread that causes this task to be completed successfully.
	 * </p><p>
	 * To access the result value of this task in the handler, use {@link #onSuccess(BooleanConsumer)}.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public TaskOfBool onSuccess(Runnable action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p><p>
	 * To access the result value of this task in the handler, use {@link #onSuccessAsync(BooleanConsumer)}.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public default TaskOfBool onSuccessAsync(Runnable action) {
		return onSuccessAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p><p>
	 * To access the result value of this task in the handler, use {@link #onSuccessAsync(BooleanConsumer, ExecutorService)}.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	@Override public TaskOfBool onSuccessAsync(Runnable action, ExecutorService executor);
	/**
	 * Adds a handler that will be run when this task fails.
	 * <p>
	 * If the task is already failed when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet done, the action will run
	 * on the thread that causes this task to fail.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task fails
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public TaskOfBool onFailure(Consumer<Throwable> action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task fails.
	 * <p>
	 * If the task is already failed when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task fails.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task fails
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public default TaskOfBool onFailureAsync(Consumer<Throwable> action) {
		return onFailureAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this task fails.
	 * <p>
	 * If the task is already failed when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task fails.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task fails
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	@Override public TaskOfBool onFailureAsync(Consumer<Throwable> action, ExecutorService executor);
	/**
	 * Adds a handler that will be run when this task completes by either being successful, cancelled or failing.
	 * The task will run in all three of these cases.
	 * <p>
	 * If the task is already done when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet done, the action will run
	 * on the thread that cancels this task or causes it to complete successfully/fail.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is done
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public TaskOfBool onCompletion(Consumer<Task> action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task completes
	 * by either being successful, cancelled or failing. The task will run in all three of these cases.
	 * <p>
	 * If the task is already done when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task is completed successfull or fails or is cancelled.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is done
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public default TaskOfBool onCompletionAsync(Consumer<Task> action) {
		return onCompletionAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run when with an {@link ExecutorService} when this task
	 * completes by either being successful, cancelled or failing. The task will run in all three of these cases.
	 * <p>
	 * If the task is already done when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task is completed successfull or fails or is cancelled.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the task is done
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	@Override public TaskOfBool onCompletionAsync(Consumer<Task> action, ExecutorService executor);
	
	/**
	 * Will return an {@link OptionalBoolean} with the result value in case the computation has finished successfully.
	 * <p>
	 * This method <b>returns {@code null}</b> if the task is still running, was cancelled or has failed.
	 * An empty optional means that the task completed successfully with a result value of {@code null}.
	 * </p>
	 * @return An {@link OptionalBoolean} with the result if the computation was successful, {@code null} otherwise
	 */
	public OptionalBoolean getFinishedResult();
	/**
	 * Will return the result value of this task in case the computation has finished successfully.
	 * <p>
	 * This method <b>returns {@code false}</b> if the task is still running, was cancelled or has failed.
	 * It may also return {@code false} when the task has completed successfully with a result value of {@code false}.
	 * </p>
	 * @return The result of this task if the computation was successful, {@code false} otherwise
	 */
	public boolean getResult(); 
	
}
