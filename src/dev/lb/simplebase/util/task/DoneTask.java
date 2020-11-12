package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * A task that is done at creation time
 */
@Internal
abstract class DoneTask<T> implements TaskOf<T> {

	@Override
	public final boolean cancel(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean cancelIfRunning(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean cancelIfNotStarted(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean isPrevented() {
		return false;
	}
	
	@Override
	public final boolean isDone() {
		return true;
	}

	@Override
	public final boolean isRunning() {
		return false;
	}

	@Override
	public final boolean isSynchronous() {
		return true;
	}
	
	@Override
	public final TaskOf<T> await() throws InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await()");
		return this;
	}

	@Override
	public final TaskOf<T> awaitUninterruptibly() {
		return this;
	}

	@Override
	public final TaskOf<T> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit)");
		return this;
	}

	@Override
	public final TaskOf<T> awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		return this;
	}

	@Override
	public final TaskOf<T> await(CancelCondition condition) throws InterruptedException, CancelledException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(CancelCondition)");
		return this;
	}

	@Override
	public final TaskOf<T> awaitUninterruptibly(CancelCondition condition) throws CancelledException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		return this;
	}

	@Override
	public final TaskOf<T> await(long timeout, TimeUnit unit, CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit, CancelCondition)");
		return this;
	}

	@Override
	public final TaskOf<T> awaitUninterruptibly(long timeout, TimeUnit unit, CancelCondition condition) throws TimeoutException, CancelledException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		return this;
	}

	@Override
	public final boolean isCancellationExpired() {
		return true;
	}
	
	@Override
	public final TaskOf<T> onCompletion(Consumer<Task> action) {
		Objects.requireNonNull(action, "'action' for onCompletion must not be null");
		action.accept(this);
		return this;
	}

	@Override
	public final TaskOf<T> onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCompletionAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCompletionAsync must not be null");
		executor.submit(() -> action.accept(this));
		return this;
	}
	
	
	@Internal
	static class CancelledTask<T> extends DoneTask<T> {

		private final CancelledException exception;
		
		CancelledTask(CancelledException exception) {
			this.exception = Objects.requireNonNull(exception, "Cancelled task must be created with an exception");
		}
		
		@Override
		public boolean isCancelled() {
			return true;
		}

		@Override
		public boolean isSuccessful() {
			return false;
		}

		@Override
		public boolean isFailed() {
			return false;
		}

		@Override
		public State getState() {
			return State.CANCELLED;
		}

		@Override
		public TaskOf<T> checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOf<T> checkFailure(Class<E> expectedType) throws E, ClassCastException {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return this;
		}

		@Override
		public Throwable getFailure() {
			return null;
		}

		@Override
		public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return null;
		}

		@Override
		public TaskOf<T> checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOf<T> onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			executor.submit(() -> action.accept(exception));
			return this;
		}

		@Override
		public TaskOf<T> onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onFailureAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onFailureAsync must not be null");
			return this;
		}

		@Override
		public boolean hasUnconsumedException() {
			return false;
		}

		@Override
		public CancelledException getCancellationException() {
			return exception;
		}

		@Override
		public TaskOf<T> onSuccess(Consumer<T> action) {
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Consumer<T> action, ExecutorService executor) {
			return this;
		}

		@Override
		public Optional<T> getFinishedResult() {
			return null;
		}

		@Override
		public T getResult() {
			return null;
		}
	}
	
	@Internal
	static class SuccessfulTask<T> extends DoneTask<T> {
		static final SuccessfulTask<Void> INSTANCE = new SuccessfulTask<>(null);
		
		private T value;
		
		SuccessfulTask(T value) {
			this.value = value;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isSuccessful() {
			return true;
		}

		@Override
		public boolean isFailed() {
			return false;
		}

		@Override
		public State getState() {
			return State.SUCCESS;
		}

		@Override
		public TaskOf<T> checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOf<T> checkFailure(Class<E> expectedType) throws E {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return this;
		}

		@Override
		public TaskOf<T> checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOf<T> onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public Throwable getFailure() {
			return null;
		}

		@Override
		public <E extends Throwable> E getFailure(Class<E> expectedType) {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return null;
		}

		@Override
		public TaskOf<T> onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			action.run();
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			executor.submit(action);
			return this;
		}

		@Override
		public TaskOf<T> onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onFailureAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onFailureAsync must not be null");
			return this;
		}

		@Override
		public boolean hasUnconsumedException() {
			return false;
		}
		
		@Override
		public CancelledException getCancellationException() {
			return null;
		}

		@Override
		public TaskOf<T> onSuccess(Consumer<T> action) {
			action.accept(value);
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Consumer<T> action, ExecutorService executor) {
			executor.submit(() -> action.accept(value));
			return this;
		}

		@Override
		public Optional<T> getFinishedResult() {
			return Optional.of(value);
		}

		@Override
		public T getResult() {
			return value;
		}
	}
	
	@Internal
	static class FailedTask<T> extends DoneTask<T> {

		private final Throwable exception;
		private final AtomicBoolean isConsumed;

		FailedTask(Throwable exception) {
			this.exception = Objects.requireNonNull(exception, "Failed task must be created with an exception");
			this.isConsumed = new AtomicBoolean(false);
		}
		
		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isSuccessful() {
			return false;
		}

		@Override
		public boolean isFailed() {
			return true;
		}

		@Override
		public State getState() {
			return State.FAILED;
		}

		@Override
		public TaskOf<T> checkFailure() throws Throwable {
			if(isConsumed.compareAndSet(false, true)) throw exception;
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> TaskOf<T> checkFailure(Class<E> expectedType) throws E {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			if(expectedType.isInstance(exception)) {
				if(isConsumed.compareAndSet(false, true)) {
					throw (E) exception;
				}
			}
			return this;
		}

		@Override
		public Throwable getFailure() {
			return exception;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return (E) exception;
		}

		@Override
		public TaskOf<T> checkSuccess() throws TaskFailureException {
			if(isConsumed.compareAndSet(false, true)) {
				throw new TaskFailureException(exception);
			}
			return this;
		}

		@Override
		public TaskOf<T> onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOf<T> onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOf<T> onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onFailureAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onFailureAsync must not be null");
			executor.submit(() -> action.accept(exception));
			return this;
		}

		@Override
		public boolean hasUnconsumedException() {
			return !isConsumed.get();
		}

		@Override
		public CancelledException getCancellationException() {
			return null;
		}

		@Override
		public TaskOf<T> onSuccess(Consumer<T> action) {
			return this;
		}

		@Override
		public TaskOf<T> onSuccessAsync(Consumer<T> action, ExecutorService executor) {
			return this;
		}

		@Override
		public Optional<T> getFinishedResult() {
			return null;
		}

		@Override
		public T getResult() {
			return null;
		}
	}
}
