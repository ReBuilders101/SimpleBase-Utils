package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class FailedTask extends DoneTask {

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
	public boolean isPrevented() {
		return false;
	}

	@Override
	public State getState() {
		return State.FAILED;
	}

	@Override
	public Task checkFailure() throws Throwable {
		if(isConsumed.compareAndSet(false, true)) throw exception;
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Task checkFailure(Class<E> expectedType) throws E {
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
	public Task checkSuccess() throws TaskFailureException {
		if(isConsumed.compareAndSet(false, true)) {
			throw new TaskFailureException(exception);
		}
		return this;
	}

	@Override
	public Task onCancelled(Consumer<TaskCancellationException> action) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<TaskCancellationException> action) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<TaskCancellationException> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
		return this;
	}

	@Override
	public Task onSuccess(Runnable action) {
		Objects.requireNonNull(action, "'action' for onSuccess must not be null");
		return this;
	}

	@Override
	public Task onSuccessAsync(Runnable action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
		return this;
	}

	@Override
	public Task onFailure(Consumer<Throwable> action) {
		Objects.requireNonNull(action, "'action' for onFailure must not be null");
		action.accept(exception);
		return this;
	}

	@Override
	public Task onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
	public boolean startAsync() throws TaskCancellationException, RejectedExecutionException {
		return false;
	}
	
	@Override
	public boolean startAsync(ExecutorService executor) throws TaskCancellationException, RejectedExecutionException {
		Objects.requireNonNull(executor, "'executor' for startAsync must not be null");
		return false;
	}

	@Override
	public boolean startSync() throws TaskCancellationException {
		return false;
	}
	
	@Override
	public boolean executeSync() throws TaskCancellationException, Throwable {
		return false;
	}
}
