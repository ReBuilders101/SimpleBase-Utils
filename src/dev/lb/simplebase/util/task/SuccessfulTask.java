package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class SuccessfulTask extends DoneTask {
	private static final AtomicBoolean singleton = new AtomicBoolean(false);
	static final SuccessfulTask INSTANCE = new SuccessfulTask();
	
	private SuccessfulTask() {
		if(!singleton.compareAndSet(false, true)) throw new IllegalStateException("Cannot create more than one instance of SuccessfulTask");
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
	public Task checkFailure() throws Throwable {
		return this;
	}

	@Override
	public <E extends Throwable> Task checkFailure(Class<E> expectedType) throws E {
		Objects.requireNonNull(expectedType, "expected exception type must not be null");
		return this;
	}

	@Override
	public Task checkSuccess() throws TaskFailureException {
		return this;
	}

	@Override
	public Task onCancelled(Consumer<CancelledException> action) {
		Objects.requireNonNull(action, "'action' for onCancelled must not be null");
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<CancelledException> action) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
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
	public Task onSuccess(Runnable action) {
		Objects.requireNonNull(action, "'action' for onSuccess must not be null");
		action.run();
		return this;
	}

	@Override
	public Task onSuccessAsync(Runnable action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
		executor.submit(action);
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
		return null;
	}
}
