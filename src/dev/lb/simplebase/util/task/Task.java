package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Threadsafe;

/**
 * Represents a potentially asynchrounous action or other blocking operation without result.
 */
@Threadsafe
public interface Task extends CancelCondition {

	//Task state flags
	/**
	 * Returns {@code true} when the task is done running, {@code false} otherwise.
	 * A task is done when it is in the states {@link State#SUCCESS}, {@link State#CANCELLED}
	 * or {@link State#FAILED}. A task that was cancelled before it was started is also considered
	 * done, as such a task will never change its state or be able to run again.
	 * @return {@code true} when the task is done running, {@code false} otherwise 
	 */
	public boolean isDone();
	/**
	 * Returns {@code true} when the task is currently running, {@code false} otherwise.
	 * A task is running only when it is in the state {@link State#RUNNING}.<br>
	 * This method is <b>not</b> the inverse of {@link #isDone()}: A task that hasn't been started
	 * yet is neither done nor running.
	 * @return {@code true} when the task is currently running, {@code false} otherwise 
	 */
	public boolean isRunning();
	/**
	 * Returns {@code true} when the task was cancelled before completion, {@code false} otherwise.
	 * A cancelled task is always done. An exception thrown from the running task does not cancel, but fail
	 * the task.
	 * @return {@code true} when the task was cancelled before completion, {@code false} otherwise
	 */
	@Override
	public boolean isCancelled();
	/**
	 * Returns {@code true} when the task completed successfully, {@code false otherwise}.
	 * A completed task is always done. The associated action has completed without exceptions and 
	 * the task was not cancelled.
	 * @return {@code true} when the task completed successfully, {@code false otherwise}
	 */
	public boolean isSuccessful();
	/**
	 * Returns {@code true} when the task failed because of an exception , {@code false} otherwise.
	 * A failed task is always done. The assoicated action threw an execption that caused it to
	 * fail.
	 * <p>
	 * The exception that caused the failure can be rethrown using {@link #checkFailure()}.
	 * </p>
	 * @return {@code true} when the task failed because of an exception , {@code false} otherwise
	 */
	public boolean isFailed();
	/**
	 * Returns {@code true} when the task is cancelled and has never run, {@code false} otherwise.
	 * A prevented task is always cancelled and done.
	 * @return {@code true} when the task is cancelled and has never run, {@code false} otherwise.
	 */
	public boolean isPrevented();
	/**
	 * Returns {@code true} when the action associated with this task was executed synchonously, {@code false} if it ran asynchrounously.
	 * A task that was cancelled, failed or successful at creation time is always synchrounous.
	 * @return {@code true} when the action associated with this task was executed synchonously, {@code false} if it ran asynchrounously.
	 */
	public boolean isSynchronous();
	
	/**
	 * The current {@link State} of this task.
	 * To check for success/failure/cancellation and other specific states
	 * use the corresponding methods instead.
	 * @return The current {@link State} of this task
	 */
	public Task.State getState();
	
	/**
	 * The state of a {@link Task}.
	 */
	public static enum State {
		/**
		 * The task has been created and initialized with a action, but has not been executed yet.
		 */
		INITIALIZED,
		/**
		 * The action associated with this task is currently running.
		 */
		RUNNING,
		/**
		 * The task is done and was cancelled before it could complete.
		 */
		CANCELLED,
		/**
		 * The task is done and executed successfully.
		 */
		SUCCESS,
		/**
		 * The task is done and failed because of an exception thrown by the action.
		 */
		FAILED;
	}
	
	//Awaiting
	/**
	 * Waits for this task to be completed.
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 */
	public Task await() throws InterruptedException;
	/**
	 * Waits for this task to be completed. Ignores interruption of the waiting thread.
	 * @return This task
	 */
	public Task awaitUninterruptibly();
	/**
	 * Waits for this task to be completed or until the timeout elapses.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public Task await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
	/**
	 * Waits for this task to be completed or until the timeout elapses. Ignores interruption of the waiting thread.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return This task
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public Task awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException;
	/**
	 * Waits for this task to be completed or until the condition is cancelled.
	 * @param condition The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws NullPointerException When {@code condition} is {@code null}
	 */
	public Task await(CancelCondition condition) throws InterruptedException, CancelledException;
	/**
	 * Waits for this task to be completed or until the condition is cancelled. Ignores interruption of the waiting thread.
	 * @param condition The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws NullPointerException When {@code condition} is {@code null}
	 */
	public Task awaitUninterruptibly(CancelCondition condition) throws CancelledException;
	/**
	 * Waits for this task to be completed or until the timeout elapses or until the condition is cancelled.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param condition The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws InterruptedException When the waiting thread is interrupted while waiting or the interrupt status is set when calling this method
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws NullPointerException When {@code unit} or {@code condition} is {@code null}
	 */
	public Task await(long timeout, TimeUnit unit, CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException;
	/**
	 * Waits for this task to be completed or until the timeout elapses or until the condition is cancelled. Ignores interruption of the waiting thread.
	 * @param timeout The maximum time to wait for completion
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param condition The {@link CancelCondition} that can be used to stop waiting for completion
	 * @return This task
	 * @throws TimeoutException When the timeout expires before the task completes
	 * @throws CancelledException When waiting is stopped by the {@link CancelCondition} (<b>not</b> when the task is cancelled)
	 * @throws NullPointerException When {@code unit} or {@code condition} is {@code null}
	 */
	public Task awaitUninterruptibly(long timeout, TimeUnit unit, CancelCondition condition) throws TimeoutException, CancelledException;
	
	/**
	 * Attempts to cancel this {@link Task}.
	 * @return {@code true} if cancellation was successful and the task stopped running, {@code false} if not
	 */
	@Override public default boolean cancel() {
		return cancel(null);
	}
	/**
	 * Attempts to cancel this {@link Task}.
	 * @param exceptionPayload A nullable object that will be available on the {@link CancelledException} caused by cancelling the task
	 * @return {@code true} if cancellation was successful and the task stopped running, {@code false} if not
	 */
	@Override public boolean cancel(Object exceptionPayload);
	
	//Get value
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
	public Task checkFailure() throws Throwable;
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
	public <E extends Throwable> Task checkFailure(Class<E> expectedType) throws E;
	/**
	 * The {@link Throwable} that caused this task to fail, if present.
	 * <p>
	 * This method will always return the same exception, even after the exception was consumed by a call to {@link #checkFailure()}, {@link #checkFailure(Class)}
	 * or {@link #checkSuccess()}.
	 * </p>
	 * @return The {@link Throwable} that caused this task to fail, if present
	 */
	public Throwable getFailure();
	/**
	 * Checks if this task still contains an unconsumed exception.
	 * <p>
	 * If the task is not in {@link State#FAILED}, this method will always return {@code false}.
	 * Otherwise, it will return {@code true} only if the exception that caused the task to fail
	 * has not yet been consumed by rethrowing it.
	 * </p>
	 * @return {@code true} if {@link #checkFailure()}, {@link #checkFailure(Class)}
	 * or {@link #checkSuccess()} can still potentially throw an exception
	 */
	public boolean hasUnconsumedException();
	/**
	 * The {@link Throwable} of type {@code E} that caused this task to fail, if present.
	 * <p>
	 * This method will always return the same exception, even after the exception was consumed by a call to {@link #checkFailure()}, {@link #checkFailure(Class)}
	 * or {@link #checkSuccess()}.
	 * </p>
	 * @param <E> The type of {@code Throwable} to check for
	 * @param expectedType The excpecetd type of the {@code Throwable}
	 * @return The {@link Throwable} that caused this task to fail, if present
	 * @throws ClassCastException When the actual exception is not an instance of {@code E}
	 * @throws NullPointerException When {@code expectedType} is {@code null}
	 */
	public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException;
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
	public Task checkSuccess() throws TaskFailureException, CancelledException;
	
	/**
	 * Attempts to cancel the associated task or blocking action only if it is currently executing.
	 * @param exceptionPayload A nullable object that will be available on the {@link CancelledException} caused by cancelling the task
	 * @return {@code true} if cancellation was successful, {@code false} if not or if the action was not running
	 */
	public boolean cancelIfRunning(Object exceptionPayload);
	/**
	 * Attempts to cancel the associated task or blocking action only if it has not yet begun executing.
	 * @param exceptionPayload A nullable object that will be available on the {@link CancelledException} caused by cancelling the task
	 * @return {@code true} if cancellation was successful, {@code false} if not or if the action was already started
	 */
	public boolean cancelIfNotStarted(Object exceptionPayload);
	
	/**
	 * Attempts to cancel the associated task or blocking action only if it is currently executing.
	 * @return {@code true} if cancellation was successful, {@code false} if not or if the action was not running
	 */
	public default boolean cancelIfRunning() {
		return cancelIfRunning(null);
	}
	/**
	 * Attempts to cancel the associated task or blocking action only if it has not yet begun executing.
	 * @return {@code true} if cancellation was successful, {@code false} if not or if the action was already started
	 */
	public default boolean cancelIfNotStarted() {
		return cancelIfNotStarted(null);
	}
	
	//Chaining
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
	@Override public Task onCancelled(Consumer<CancelledException> action);
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
	@Override public default Task onCancelledAsync(Consumer<CancelledException> action) {
		return onCancelledAsync(action, defaultExecutor());
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
	@Override public Task onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor);
	
	/**
	 * Adds a handler that will be run when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet done, the action will run
	 * on the thread that causes this task to be completed successfully.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public Task onSuccess(Runnable action);
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @return This task
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public default Task onSuccessAsync(Runnable action) {
		return onSuccessAsync(action, defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this task completes successfully.
	 * <p>
	 * If the task is already done successfully when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet done, the action will
	 * be submitted to the executor when the task completes successfully.
	 * </p>
	 * @param action The {@link Runnable} that will be run when the task completes successfully
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This task
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	public Task onSuccessAsync(Runnable action, ExecutorService executor);
	
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
	public Task onFailure(Consumer<Throwable> action);
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
	public default Task onFailureAsync(Consumer<Throwable> action) {
		return onFailureAsync(action, defaultExecutor());
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
	public Task onFailureAsync(Consumer<Throwable> action, ExecutorService executor);
	
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
	public Task onCompletion(Consumer<Task> action);
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
	public default Task onCompletionAsync(Consumer<Task> action) {
		return onCompletionAsync(action, defaultExecutor());
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
	public Task onCompletionAsync(Consumer<Task> action, ExecutorService executor);
	
	/**
	 * The {@link ExecutorService} used when registering asynchronous handlers (
	 * {@link #onSuccessAsync(Runnable)}, {@link #onFailureAsync(Consumer)}, {@link #onFailureAsync(Consumer)}, {@link #onCompletionAsync(Consumer)}
	 * ) without supplying an executor.
	 * @return The default {@link ExecutorService} for tasks and handlers
	 */
	public static ExecutorService defaultExecutor() {
		return ForkJoinPool.commonPool();
	}
	
	/**
	 * Should be called before the application exits to allow all asynchonously executing tasks
	 * to finish properly. 
	 * @param timeout The maximum time to wait for all tasks to complete
	 * @param unit The {@link TimeUnit} for the timeout
	 */
	public static void awaitCompletion(long timeout, TimeUnit unit) {
		ForkJoinPool.commonPool().awaitQuiescence(timeout, unit);
	}
}
