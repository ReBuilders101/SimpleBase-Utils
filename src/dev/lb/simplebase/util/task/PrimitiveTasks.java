package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Out;
import dev.lb.simplebase.util.annotation.StaticType;
import dev.lb.simplebase.util.function.BooleanFunction;
import dev.lb.simplebase.util.task.Task.State;
import dev.lb.simplebase.util.timer.GlobalTimer;

/**
 * Method equivalents to the methods in {@link Tasks}, but for primitive task types
 */
@StaticType
public final class PrimitiveTasks {
	private PrimitiveTasks() {}
	
	/**
	 * Creates a task that is immediately successful.
	 * @param value The result value
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static TaskOfBool successBool(boolean value) {
		return value ? DoneTaskOfBool.SuccessfulTaskOfBool.TRUE_INSTANCE : DoneTaskOfBool.SuccessfulTaskOfBool.FALSE_INSTANCE;
	}
	
	/**
	 * Creates a task that is immediately failed with an exception.
	 * @param cause The {@link Throwable} that caused the failure
	 * @return A {@link Task} that is always in state {@link State#FAILED}
	 */
	public static TaskOfBool failedBool(Throwable cause) {
		return new DoneTaskOfBool.FailedTaskOfBool(cause);
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static TaskOfBool cancelledBool() {
		return new DoneTaskOfBool.CancelledTaskOfBool(new CancelledException(null));
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param payload The object associated with the cancellation
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static TaskOfBool cancelledBool(Object payload) {
		return new DoneTaskOfBool.CancelledTaskOfBool(new CancelledException(payload));
	}
	
	/**
	 * Creates a {@link TaskOfBool} that will wait until a {@link TaskCompleterOfBool} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link TaskOf#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param completionSource <i>&#064;</i> The {@link TaskCompleterOfBool} that will complete the task
	 * @return A {@link TaskOfBool} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws OutParamStateException When the {@code completionSource} was already used to construct another task
	 */
	public static TaskOfBool startBlockingBool(@Out TaskCompleterOfBool completionSource) throws OutParamStateException {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		try {
			return new BlockingTaskOfBool.ConditionWaiterTaskOfBool(completionSource);
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInBool(TaskOfBool inner, BooleanFunction<? extends V> operation) {
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInAsyncBool(TaskOfBool inner, BooleanFunction<? extends V> operation) {
		return chainInAsyncBool(inner, operation, Task.defaultExecutor());
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @param executor The {@link ExecutorService} that should run the operation
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInAsyncBool(TaskOfBool inner, BooleanFunction<? extends V> operation, ExecutorService executor) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		TaskCompleterOf<V> tco = TaskCompleterOf.create();
		TaskOf<V> resultTask = Tasks.startBlocking(tco);
		inner.onSuccessAsync(value -> tco.signalSuccess(operation.apply(value)), executor);
		inner.onFailureAsync(thrbl -> tco.signalFailure(thrbl), executor);
		inner.onCancelledAsync(canex -> resultTask.cancel(canex.getPayload()), executor);
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
	 * The chained operation runs on the thread that completes the inner task.
	 * </p>
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfBool chainOutBool(TaskOf<V> inner, Predicate<? super V> operation) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		TaskCompleterOfBool tco = TaskCompleterOfBool.create();
		TaskOfBool resultTask = PrimitiveTasks.startBlockingBool(tco);
		inner.onSuccess(value -> {
			try {
				tco.signalSuccess(operation.test(value));
			} catch (Throwable e) {
				tco.signalFailure(new ExecutionException(e));
			}
		});
		inner.onFailure(thrbl -> tco.signalFailure(thrbl));
		inner.onCancelled(canex -> resultTask.cancel(canex.getPayload()));
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
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfBool chainOutAsyncBool(TaskOf<V> inner, Predicate<? super V> operation) {
		return chainOutAsyncBool(inner, operation, Task.defaultExecutor());
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
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @param executor The {@link ExecutorService} that should run the operation
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfBool chainOutAsyncBool(TaskOf<V> inner, Predicate<? super V> operation, ExecutorService executor) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		TaskCompleterOfBool tco = TaskCompleterOfBool.create();
		TaskOfBool resultTask = PrimitiveTasks.startBlockingBool(tco);
		inner.onSuccessAsync(value -> tco.signalSuccess(operation.test(value)), executor);
		inner.onFailureAsync(thrbl -> tco.signalFailure(thrbl), executor);
		inner.onCancelledAsync(canex -> resultTask.cancel(canex.getPayload()), executor);
		return resultTask;
	}
	
	/**
	 * Creates and starts a {@link Task} that will be cancelled after the specified timeout elapses.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param cancellationPayload The nullable object that will be included with the {@link CancelledException}.
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will be cancelled after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static TaskOfBool cancelAfterBool(Object cancellationPayload, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfBool completer = TaskCompleterOfBool.create(); //Never use the completer
		final TaskOfBool delayed = new BlockingTaskOfBool.ConditionWaiterTaskOfBool(completer);
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
	 * @param failureReason The {@link Throwable} that caused the action to fail
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will fail after the timeout elapses
	 * @throws NullPointerException When {@code failureReason} or {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static TaskOfBool failAfterBool(Throwable failureReason, long timeout, TimeUnit unit) {
		Objects.requireNonNull(failureReason, "'failureReason' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfBool completer = TaskCompleterOfBool.create(); //Never use the completer
		final TaskOfBool delayed = new BlockingTaskOfBool.ConditionWaiterTaskOfBool(completer);
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
	 * @param value The result value that the task will have
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public static TaskOfBool succeedAfterBool(boolean value, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfBool completer = TaskCompleterOfBool.create();
		final TaskOfBool delayed = new BlockingTaskOfBool.ConditionWaiterTaskOfBool(completer);
		GlobalTimer.scheduleOnce(() -> completer.signalSuccess(value), timeout, unit);
		return delayed;
	}
	
	
	//////////////////////// INTS ////////////////////////////////////////////
	
	/**
	 * Creates a task that is immediately successful.
	 * @param value The result value
	 * @return A {@link Task} that always is in state {@link State#SUCCESS}
	 */
	public static TaskOfInt successInt(int value) {
		return new DoneTaskOfInt.SuccessfulTaskOfInt(value);
	}
	
	/**
	 * Creates a task that is immediately failed with an exception.
	 * @param cause The {@link Throwable} that caused the failure
	 * @return A {@link Task} that is always in state {@link State#FAILED}
	 */
	public static TaskOfInt failedInt(Throwable cause) {
		return new DoneTaskOfInt.FailedTaskOfInt(cause);
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static TaskOfInt cancelledInt() {
		return new DoneTaskOfInt.CancelledTaskOfInt(new CancelledException(null));
	}
	
	/**
	 * Creates a task that is immediately cancelled.
	 * @param payload The object associated with the cancellation
	 * @return A {@link Task} that is always in state {@link State#CANCELLED}
	 */
	public static TaskOfInt cancelledInt(Object payload) {
		return new DoneTaskOfInt.CancelledTaskOfInt(new CancelledException(payload));
	}
	
	/**
	 * Creates a {@link TaskOfInt} that will wait until a {@link TaskCompleterOfInt} is signalled.
	 * <p>
	 * <b>Important:</b> The condition object should be created explicitly for this task, as
	 * user actions done on the task may also signal the condition (e.g. a call to {@link TaskOf#cancel()}
	 * will signal the condition to resume waiting threads).
	 * </p>
	 * @param completionSource <i>&#064;Out</i> The {@link TaskCompleterOfInt} that will complete the task
	 * @return A {@link TaskOfInt} that will complete when the completion source is signalled
	 * @throws NullPointerException When {@code completionSource} is {@code null}
	 * @throws OutParamStateException When the {@code completionSource} was already used to construct another task
	 */
	public static TaskOfInt startBlockingInt(@Out TaskCompleterOfInt completionSource) throws OutParamStateException {
		Objects.requireNonNull(completionSource, "'completionSource' parameter must not be null");
		try {
			return new BlockingTaskOfInt.ConditionWaiterTaskOfInt(completionSource);
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInInt(TaskOfInt inner, IntFunction<? extends V> operation) {
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInAsyncInt(TaskOfInt inner, IntFunction<? extends V> operation) {
		return chainInAsyncInt(inner, operation, Task.defaultExecutor());
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
	 * @param <V> The result type of the outer task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @param executor The {@link ExecutorService} that should run the operation
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOf<V> chainInAsyncInt(TaskOfInt inner, IntFunction<? extends V> operation, ExecutorService executor) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		TaskCompleterOf<V> tco = TaskCompleterOf.create();
		TaskOf<V> resultTask = Tasks.startBlocking(tco);
		inner.onSuccessAsync(value -> tco.signalSuccess(operation.apply(value)), executor);
		inner.onFailureAsync(thrbl -> tco.signalFailure(thrbl), executor);
		inner.onCancelledAsync(canex -> resultTask.cancel(canex.getPayload()), executor);
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
	 * The chained operation runs on the thread that completes the inner task.
	 * </p>
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfInt chainOutInt(TaskOf<V> inner, ToIntFunction<? super V> operation) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		TaskCompleterOfInt tco = TaskCompleterOfInt.create();
		TaskOfInt resultTask = PrimitiveTasks.startBlockingInt(tco);
		inner.onSuccess(value -> {
			try {
				tco.signalSuccess(operation.applyAsInt(value));
			} catch (Throwable e) {
				tco.signalFailure(new ExecutionException(e));
			}
		});
		inner.onFailure(thrbl -> tco.signalFailure(thrbl));
		inner.onCancelled(canex -> resultTask.cancel(canex.getPayload()));
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
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfInt chainOutAsyncInt(TaskOf<V> inner, ToIntFunction<? super V> operation) {
		return chainOutAsyncInt(inner, operation, Task.defaultExecutor());
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
	 * @param <V> The result type of the inner task
	 * @param inner The inner task that supplies the result for the function
	 * @param operation A {@link Function} that maps the inner result to the outer result
	 * @param executor The {@link ExecutorService} that should run the operation
	 * @return A {@link Task} that waits for the inner task to finish and then applies a function to the result.
	 */
	public static <V> TaskOfInt chainOutAsyncInt(TaskOf<V> inner, ToIntFunction<? super V> operation, ExecutorService executor) {
		Objects.requireNonNull(inner, "'inner' parameter must not be null");
		Objects.requireNonNull(operation, "'operation' parameter must not be null");
		Objects.requireNonNull(executor, "'executor' parameter must not be null");
		TaskCompleterOfInt tco = TaskCompleterOfInt.create();
		TaskOfInt resultTask = PrimitiveTasks.startBlockingInt(tco);
		inner.onSuccessAsync(value -> tco.signalSuccess(operation.applyAsInt(value)), executor);
		inner.onFailureAsync(thrbl -> tco.signalFailure(thrbl), executor);
		inner.onCancelledAsync(canex -> resultTask.cancel(canex.getPayload()), executor);
		return resultTask;
	}
	
	/**
	 * Creates and starts a {@link Task} that will be cancelled after the specified timeout elapses.
	 * <p>
	 * Unlike most other {@link Task} implementations, the returned task will use a global timer
	 * thread to manage the waiting time. This avoids using an entire thread from an {@link ExecutionException}
	 * only to block that thread with {@link Thread#sleep(long)}.
	 * </p>
	 * @param cancellationPayload The nullable object that will be included with the {@link CancelledException}.
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will be cancelled after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static TaskOfInt cancelAfterInt(Object cancellationPayload, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfInt completer = TaskCompleterOfInt.create(); //Never use the completer
		final TaskOfInt delayed = new BlockingTaskOfInt.ConditionWaiterTaskOfInt(completer);
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
	 * @param failureReason The {@link Throwable} that caused the action to fail
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will fail after the timeout elapses
	 * @throws NullPointerException When {@code failureReason} or {@code unit} is {@code null}
	 * @throws RejectedExecutionException When the action that completes the {@link Task} after the timeout elapses could not be scheduled
	 * on the global timer thread pool using {@link GlobalTimer#scheduleOnce(Runnable, long, TimeUnit)}
	 */
	public static TaskOfInt failAfterInt(Throwable failureReason, long timeout, TimeUnit unit) {
		Objects.requireNonNull(failureReason, "'failureReason' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfInt completer = TaskCompleterOfInt.create(); //Never use the completer
		final TaskOfInt delayed = new BlockingTaskOfInt.ConditionWaiterTaskOfInt(completer);
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
	 * @param value The result value that the task will have
	 * @param timeout The timeout the task taskes to complete 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A task that will complete after the timeout elapses
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public static TaskOfInt succeedAfterInt(int value, long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		final TaskCompleterOfInt completer = TaskCompleterOfInt.create();
		final TaskOfInt delayed = new BlockingTaskOfInt.ConditionWaiterTaskOfInt(completer);
		GlobalTimer.scheduleOnce(() -> completer.signalSuccess(value), timeout, unit);
		return delayed;
	}
}
