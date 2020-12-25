package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Out;
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
	 * Creates a {@link Future} representing the {@link TaskOf}.
	 * @param <T> The type of result for the task and the future
	 * @param task The task to wrap in a future
	 * @return The furture that represents the task
	 */
	public static <T> Future<T> taskOfToFuture(TaskOf<T> task) {
		return new TaskOfFutureImpl<>(task);
	}
	
	/**
	 * Creates a task that is immediately successful.
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static TaskOf<Void> success() {
		return DoneTaskOf.SuccessfulTaskOf.INSTANCE;
	}
	
	/**
	 * Creates a task that is immediately successful.
	 * @param <T> The type of the task result
	 * @param value The result value
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static <T> TaskOf<T> success(T value) {
		return new DoneTaskOf.SuccessfulTaskOf<>(value);
	}
	
	/**
	 * Creates a task that is immediately failed with an exception.
	 * @param <T> The type of the task result
	 * @param cause The {@link Throwable} that caused the failure
	 * @return A {@link Task} that is always in state {@link State#FAILED}
	 */
	public static <T> TaskOf<T> failed(Throwable cause) {
		return new DoneTaskOf.FailedTaskOf<>(cause);
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param <T> The type of the task result
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static <T> TaskOf<T> cancelled() {
		return new DoneTaskOf.CancelledTaskOf<>(new CancelledException("Task cancelled at creation time", null));
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param <T> The type of the task result
	 * @param payload The object associated with the cancellation
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static <T> TaskOf<T> cancelled(Object payload) {
		return new DoneTaskOf.CancelledTaskOf<>(new CancelledException("Task cancelled at creation time", payload));
	}
	
	/**
	 * Creates a {@link Task} that will wait until a {@link TaskCompleter} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link Task#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param completionSource <i>&#064;Out</i> The {@link TaskCompleter} that will complete the task
	 * @return A {@link Task} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws OutParamStateException When the {@code completionSource} was already used to construct another task
	 */
	public static Task startBlocking(@Out TaskCompleter completionSource) throws OutParamStateException {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		try {
			return new BlockingTaskOf.ConditionWaiterTaskOf<>(completionSource.inner());
		} catch (IllegalArgumentException e) {
			throw new OutParamStateException("'completionSource' parameter was already used for another task", e);
		}
	}
	
	/**
	 * Creates a {@link TaskOf} that will wait until a {@link TaskCompleterOf} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link TaskOf#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param <T> The result type of the task
	 * @param completionSource <i>&#064;Out</i> The {@link TaskCompleterOf} that will complete the task
	 * @return A {@link TaskOf} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws OutParamStateException When the {@code completionSource} was already used to construct another task
	 */
	public static <T> TaskOf<T> startBlocking(@Out TaskCompleterOf<T> completionSource) throws OutParamStateException {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		try {
			return new BlockingTaskOf.ConditionWaiterTaskOf<>(completionSource);
		} catch (IllegalArgumentException e) {
			throw new OutParamStateException("'completionSource' parameter was already used for another task", e);
		}
	}
	
	/**
	 * Creates a new {@link Task} that contains the result of another task with a {@link Function} applied to it.
	 * <p>
	 * If the inner task succeeds, the returned task will succeed with the processed result.<br>
	 * If the inner task fails with an exception, the returned task will fail with the same exception (same {@link Throwable} instance).<br>
	 * If the inner task is cancelled, the returned task will be cancelled with the same payload (but a different {@link CancellationException}).<br>
	 * </p><p>
	 * If the {@link Function} throws an unchecked exception, the outer task will fail with an {@link ExecutionException}
	 * wrapping that unchecked exception, even if the inner task succeeded.
	 * </p><p>
	 * The chained operation runs on the thread that completes the inner task.
	 * </p>
	 * @param <I> The result type of the inner task
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <I, V> TaskOf<V> chain(TaskOf<I> inner, Function<? super I, ? extends V> operation) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		TaskCompleterOf<V> tco = TaskCompleterOf.create();
		TaskOf<V> resultTask = Tasks.startBlocking(tco);
		inner.onSuccess(value -> {
			try {
				tco.signalSuccess(operation.apply(value));
			} catch (Throwable e) {
				tco.signalFailure(new ExecutionException(e));
			}
		});
		inner.onFailure(thrbl -> tco.signalFailure(thrbl));
		inner.onCancelled(canex -> resultTask.cancel(canex.getPayload()));
		//Outer can also cancel inner
		resultTask.onCancelled(canex -> inner.cancel(canex.getPayload()));
		return resultTask;
	}
	
	/**
	 * Creates a new {@link Task} that contains the result of another task with a {@link Function} applied to it.
	 * <p>
	 * If the inner task succeeds, the returned task will succeed with the processed result.<br>
	 * If the inner task fails with an exception, the returned task will fail with the same exception (same {@link Throwable} instance).<br>
	 * If the inner task is cancelled, the returned task will be cancelled with the same payload (but a different {@link CancellationException}).<br>
	 * </p><p>
	 * If the {@link Function} throws an unchecked exception, the outer task will fail with an {@link ExecutionException}
	 * wrapping that unchecked exception, even if the inner task succeeded.
	 * </p><p>
	 * The chained operation runs on a thread in the {@link Task#defaultExecutor()} thread pool.
	 * </p>
	 * @param <I> The result type of the inner task
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <I, V> TaskOf<V> chainAsync(TaskOf<I> inner, Function<? super I, ? extends V> operation) {
		return chainAsync(inner, operation, Task.defaultExecutor());
	}
	
	/**
	 * Creates a new {@link Task} that contains the result of another task with a {@link Function} applied to it.
	 * <p>
	 * If the inner task succeeds, the returned task will succeed with the processed result.<br>
	 * If the inner task fails with an exception, the returned task will fail with the same exception (same {@link Throwable} instance).<br>
	 * If the inner task is cancelled, the returned task will be cancelled with the same payload (but a different {@link CancellationException}).<br>
	 * </p><p>
	 * If the {@link Function} throws an unchecked exception, the outer task will fail with an {@link ExecutionException}
	 * wrapping that unchecked exception, even if the inner task succeeded.
	 * </p><p>
	 * The chained operation runs on a thread in the {@code executor}s thread pool
	 * </p>
	 * @param <I> The result type of the inner task
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @param executor The {@link ExecutorService} that should run the operation
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <I, V> TaskOf<V> chainAsync(TaskOf<I> inner, Function<? super I, ? extends V> operation, ExecutorService executor) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		TaskCompleterOf<V> tco = TaskCompleterOf.create();
		TaskOf<V> resultTask = Tasks.startBlocking(tco);
		inner.onSuccessAsync(value -> tco.signalSuccess(operation.apply(value)), executor);
		inner.onFailureAsync(thrbl -> tco.signalFailure(thrbl), executor);
		inner.onCancelledAsync(canex -> resultTask.cancel(canex.getPayload()), executor);
		resultTask.onCancelledAsync(canex -> inner.cancel(canex.getPayload()), executor);
		return resultTask;
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
	public static Task delay(long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOf<Void> completer = TaskCompleterOf.create();
		final TaskOf<Void> delayed = new BlockingTaskOf.ConditionWaiterTaskOf<>(completer);
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
	public static Task waiting() {
		final TaskCompleterOf<Void> completer = TaskCompleterOf.create(); //Never use the completer
		final TaskOf<Void> delayed = new BlockingTaskOf.ConditionWaiterTaskOf<>(completer);
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
		final TaskOf<T> delayed = new BlockingTaskOf.ConditionWaiterTaskOf<>(completer);
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
		final TaskOf<T> delayed = new BlockingTaskOf.ConditionWaiterTaskOf<>(completer);
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
		final TaskOf<T> delayed = new BlockingTaskOf.ConditionWaiterTaskOf<>(completer);
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
	
	@Internal
	private static class TaskOfFutureImpl<T> implements Future<T> {
		private final TaskOf<T> task;
		
		private TaskOfFutureImpl(TaskOf<T> task) {
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
		public T get() throws InterruptedException, ExecutionException {
			try {
				task.await().checkFailure();
				return task.getResult();
			} catch (InterruptedException e) {
				throw e; //Rethrow
			} catch (Throwable e) {
				throw new ExecutionException(e); //Wrap
			}
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			try {
				task.await(timeout, unit).checkFailure();
				return task.getResult();
			} catch (InterruptedException | TimeoutException e) {
				throw e; //Rethrow
			} catch (Throwable e) {
				throw new ExecutionException(e); //Wrap
			}
		}
		
	}
}
