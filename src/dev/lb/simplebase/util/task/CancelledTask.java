package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
class CancelledTask extends DoneTask {

	private final boolean prevented;
	private final TaskCancellationException exception;
	
	CancelledTask(boolean prevented, TaskCancellationException exception) {
		this.prevented = prevented;
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
	public boolean isPrevented() {
		return prevented;
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
	public Task onCancelled(Consumer<TaskCancellationException> action) {
		Objects.requireNonNull(action, "'action' for onCancelled must not be null");
		action.accept(exception);
		return this;
	}

	@Override
	public Task onCancelledAsync(Consumer<TaskCancellationException> action, ExecutorService executor) {
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
	public final boolean startAsync() throws TaskCancellationException, RejectedExecutionException {
		if(prevented) throw exception;
		return false;
	}
	
	@Override
	public final boolean startAsync(ExecutorService executor) throws TaskCancellationException, RejectedExecutionException {
		Objects.requireNonNull(executor, "'executor' for startAsync must not be null");
		if(prevented) throw exception;
		return false;
	}

	@Override
	public boolean startSync() throws TaskCancellationException {
		if(prevented) throw exception;
		return false;
	}
	
	@Override
	public boolean executeSync() throws TaskCancellationException, Throwable {
		if(prevented) throw exception;
		return false;
	}
}
