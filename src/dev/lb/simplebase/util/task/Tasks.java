package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.StaticType;
import dev.lb.simplebase.util.task.Task.State;

/**
 * Contains methods fro creating and converting {@link Task}s
 */
@StaticType
public final class Tasks {
	private Tasks() {}
	
	/**
	 * Creates a {@link Future} representing the {@link Task}.
	 * @param task The task to wrap in a future
	 * @return The furture that represents the task
	 */
	public static Future<Void> taskToFuture(Task task) {
		return new TaskFutureImpl(task);
	}
	
	/**
	 * Creates a {@link Task} representing a {@link Future}.
	 * Not all features of a task are supported by a future.
	 * @param future The future to wrap in a task
	 * @return The task that represents the future
	 */
	public static Task futureToTask(Future<?> future) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Creates a task that is immediately successful.
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static Task success() {
		return SuccessfulTask.INSTANCE;
	}
	
	/**
	 * Creates a task that is immediately failed with an exception.
	 * @param cause The {@link Throwable} that caused the failure
	 * @return A {@link Task} that is always in state {@link State#FAILED}
	 */
	public static Task failed(Throwable cause) {
		return new FailedTask(cause);
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static Task cancelled() {
		return new CancelledTask(new CancelledException(null));
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param payload The object associated with the cancellation
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static Task cancelled(Object payload) {
		return new CancelledTask(new CancelledException(payload));
	}
	
	/**
	 * Creates a {@link Task} representing a {@link TaskAction}.
	 * @param action The action to represent
	 * @return The {@link Task} that represents the action
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public static Task createAction(TaskAction action) {
		Objects.requireNonNull(action, "'action' parameter must not be null");
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Creates a {@link Task} representing a {@link TaskAction} and 
	 * starts that task synchronously.<br>
	 * Equivalent to calling {@link Task#startSync()} on a task produced by
	 * {@link #createAction(TaskAction)} using the same action.
	 * <p>
	 * Because the task is running synchronously, it will be done (completed, cancelled or failed)
	 * by the time this method returns.
	 * </p>
	 * @param action The action to represent
	 * @return The started and completed {@link Task} that represents the action
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public static Task startActionSync(TaskAction action) {
		Objects.requireNonNull(action, "'action' parameter must not be null");
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Creates a {@link Task} representing a {@link TaskAction} and 
	 * starts that task asynchronously using the {@link Task#defaultExecutor()}.<br>
	 * Equivalent to calling {@link Task#startAsync()} on a task produced by
	 * {@link #createAction(TaskAction)} using the same action.
	 * @param action The action to represent
	 * @return The started {@link Task} that represents the action
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public static Task startActionAsync(TaskAction action) {
		Objects.requireNonNull(action, "'action' parameter must not be null");
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Creates a {@link Task} representing a {@link TaskAction} and 
	 * starts that task asynchronously using an {@link ExecutorService}.<br>
	 * Equivalent to calling {@link Task#startAsync()} on a task produced by
	 * {@link #createAction(TaskAction)} using the same action.
	 * @param action The action to represent
	 * @param executor The {@link ExecutorService} that will run the action
	 * @return The started {@link Task} that represents the action
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	public static Task startActionAsync(TaskAction action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Creates a {@link Task} that will wait until a {@link TaskCompleter} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link Task#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param completionSource The {@link TaskCompleter} that will complete the task
	 * @return A {@link Task} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code lock} or {@code waitingCondition} is {@code null}
	 * @throws IllegalArgumentException When the {@code completionSource} was already used to construct another task
	 */
	public static Task startBlocking(TaskCompleter completionSource) {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		return new ConditionWaiterTask(completionSource);
	}
	
	/**
	 * Creates an starts a {@link Task} that completes after the specified timeout elapses.
	 * The task can be cancelled by calling {@link Task#cancel()}. It can (rarely) fail
	 * because the global timer thread is forcibly shut down using an interrrupt, without allowing all tasks
	 * to complete normally.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p><p>
	 * <b>Caution:</b> A synchronous completion/cancellation handler for this task will run on the 
	 * global timer thread. blocking this thread prevents other timed tasks to be executed properly.
	 * It is recommended to only use asynchronous handlers with this task.
	 * </p>
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public static Task delay(long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Creates an starts a {@link Task} that completes after the specified timeout elapses.
	 * The task can be cancelled by calling {@link Task#cancel()}. It can fail
	 * with an {@link InterruptedException} when the blocked thread is interrupted.
	 * <p>
	 * Because it is implemented using {@link Thread#sleep(long)}, using this task will
	 * block a entire thread of the {@link ExecutorService} until it completed. It 
	 * is recommended to use {@link #delay(long, TimeUnit)} instead.
	 * </p>
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param executor The {@link ExecutorService} that provides a thread to block for the waiting time
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} or {@code service} is {@code null}
	 * @deprecated This method will block the executor thread while waiting. Use {@link #delay(long, TimeUnit)} instead.
	 */
	@Deprecated
	public static Task delayExecutor(long timeout, TimeUnit unit, ExecutorService executor) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		return startActionAsync((ctx) -> Thread.sleep(unit.toMillis(timeout)), executor);
	}
	
	@Internal
	private static class TaskFutureImpl implements Future<Void> {
		private final Task task;
		
		private TaskFutureImpl(Task task) {
			this.task = task;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if(mayInterruptIfRunning) {
				return task.cancel();
			} else {
				return task.cancelIfNotStarted();
			}
		}

		@Override
		public boolean isCancelled() {
			return task.isCancelled();
		}

		@Override
		public boolean isDone() {
			return task.isDone();
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			try {
				task.await().checkFailure();
				return null;
			} catch (InterruptedException e) {
				throw e; //Rethrow
			} catch (Throwable e) {
				throw new ExecutionException(e); //Wrap
			}
		}

		@Override
		public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			try {
				task.await(timeout, unit).checkFailure();
				return null;
			} catch (InterruptedException | TimeoutException e) {
				throw e; //Rethrow
			} catch (Throwable e) {
				throw new ExecutionException(e); //Wrap
			}
		}
		
	}
}
