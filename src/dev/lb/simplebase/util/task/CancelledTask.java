package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class CancelledTask extends DoneTask {

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
	public Task checkFailure() throws Throwable {
		return this;
	}

	@Override
	public <E extends Throwable> Task checkFailure(Class<E> expectedType) throws E, ClassCastException {
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
	public Task checkSuccess() throws TaskFailureException {
		return this;
	}

	@Override
	public Task onCancelled(Consumer<CancelledException> action) {
		Objects.requireNonNull(action, "'action' for onCancelled must not be null");
		action.accept(exception);
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
		executor.submit(() -> action.accept(exception));
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
		return this;
	}

	@Override
	public Task onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
}
