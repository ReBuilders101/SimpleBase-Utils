package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.StaticType;
import dev.lb.simplebase.util.task.Task.State;
import dev.lb.simplebase.util.timer.GlobalTimer;

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
	 * Creates a task that is immediately successful.
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static TaskOf<Void> success() {
		return DoneTask.SuccessfulTask.INSTANCE;
	}
	
	/**
	 * Creates a task that is immediately successful.
	 * @param <T> The type of the task result
	 * @param value The result value
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static <T> TaskOf<T> success(T value) {
		return new DoneTask.SuccessfulTask<>(value);
	}
	
	/**
	 * Creates a task that is immediately failed with an exception.
	 * @param <T> The type of the task result
	 * @param cause The {@link Throwable} that caused the failure
	 * @return A {@link Task} that is always in state {@link State#FAILED}
	 */
	public static <T> TaskOf<T> failed(Throwable cause) {
		return new DoneTask.FailedTask<>(cause);
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param <T> The type of the task result
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static <T> TaskOf<T> cancelled() {
		return new DoneTask.CancelledTask<>(new CancelledException(null));
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param <T> The type of the task result
	 * @param payload The object associated with the cancellation
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static <T> TaskOf<T> cancelled(Object payload) {
		return new DoneTask.CancelledTask<>(new CancelledException(payload));
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
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws IllegalArgumentException When the {@code completionSource} was already used to construct another task
	 */
	public static TaskOf<Void> startBlocking(TaskCompleter completionSource) {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		return new ConditionWaiterTask<>(completionSource.inner());
	}
	
	/**
	 * Creates a {@link TaskOf} that will wait until a {@link TaskCompleterOf} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link TaskOf#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param <T> The result type of the task
	 * @param completionSource The {@link TaskCompleterOf} that will complete the task
	 * @return A {@link TaskOf} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws IllegalArgumentException When the {@code completionSource} was already used to construct another task
	 */
	public static <T> TaskOf<T> startBlocking(TaskCompleterOf<T> completionSource) {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		return new ConditionWaiterTask<>(completionSource);
	}
	
	/**
	 * Creates an starts a {@link Task} that completes after the specified timeout elapses.
	 * The task can be cancelled by calling {@link Task#cancel()}.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static TaskOf<Void> delay(long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOf<Void> completer = TaskCompleterOf.create();
		final TaskOf<Void> delayed = new ConditionWaiterTask<>(completer);
		GlobalTimer.scheduleOnce(() -> completer.signalSuccess(null), timeout, unit);
		return delayed;
	}
	
	/**
	 * Creates and starts a {@link Task} that will wait indefinitely, unless cancelled by invocation
	 * of the returned tasks {@link Task#cancel()} method (and the other {@code cancel...(...)} methods).
	 * <p>
	 * It should be considered whether using an {@link Awaiter} diectly is more appropiate.
	 * </p>
	 * @return A task that never completes on its own, but can be cancelled
	 */
	public static TaskOf<Void> waiting() {
		final TaskCompleterOf<Void> completer = TaskCompleterOf.create(); //Never use the completer
		final TaskOf<Void> delayed = new ConditionWaiterTask<>(completer);
		return delayed;
	}
	
	/**
	 * Creates and starts a {@link Task} that will be cancelled after the specified timeout elapses.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param <T> The type of the task result
	 * @param cancellationPayload The nullable object that will be included with the {@link CancelledException}.
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will be cancelled after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static <T> TaskOf<T> cancelAfter(Object cancellationPayload, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOf<T> completer = TaskCompleterOf.create(); //Never use the completer
		final TaskOf<T> delayed = new ConditionWaiterTask<>(completer);
		GlobalTimer.scheduleOnce(() -> delayed.cancel(cancellationPayload), timeout, unit);
		return delayed;
	}
	
	/**
	 * Creates and starts a {@link Task} that will fail after the specified timeout elapses.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param <T> The type of the task result
	 * @param failureReason The {@link Throwable} that caused the action to fail
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will fail after the timeout elapses
	 * @throws NullPointerException When {@code failureReason} or {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static <T> TaskOf<T> failAfter(Throwable failureReason, long timeout, TimeUnit unit) {
		Objects.requireNonNull(failureReason, "'failureReason' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOf<T> completer = TaskCompleterOf.create(); //Never use the completer
		final TaskOf<T> delayed = new ConditionWaiterTask<>(completer);
		GlobalTimer.scheduleOnce(() -> completer.signalFailure(failureReason), timeout, unit);
		return delayed;
	}
	
	/**
	 * <p>
	 * Creates an starts a {@link Task} that completes after the specified timeout elapses.
	 * The task can be cancelled by calling {@link Task#cancel()}. It can (rarely) fail
	 * because the global timer thread is forcibly shut down using an interrrupt, without allowing all tasks
	 * to complete normally.
	 * </p><p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param <T> The type of the task result
	 * @param value The result value that the task will have
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public static <T> TaskOf<T> succeedAfter(T value, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOf<T> completer = TaskCompleterOf.create();
		final TaskOf<T> delayed = new ConditionWaiterTask<>(completer);
		GlobalTimer.scheduleOnce(() -> completer.signalSuccess(value), timeout, unit);
		return delayed;
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
