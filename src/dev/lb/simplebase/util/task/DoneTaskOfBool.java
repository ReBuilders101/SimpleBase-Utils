package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Out;
import dev.lb.simplebase.util.function.BooleanConsumer;
import dev.lb.simplebase.util.value.OptionalBoolean;

@Internal
abstract class DoneTaskOfBool implements TaskOfBool {
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
	public final TaskOfBool await() throws InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await()");
		return this;
	}

	@Override
	public final TaskOfBool awaitUninterruptibly() {
		return this;
	}

	@Override
	public final TaskOfBool await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit)");
		return this;
	}

	@Override
	public final TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		return this;
	}

	@Override
	public final TaskOfBool await(@Out CancelCondition condition) throws InterruptedException, CancelledException, OutParamStateException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(CancelCondition)");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfBool awaitUninterruptibly(@Out CancelCondition condition) throws CancelledException, OutParamStateException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfBool await(long timeout, TimeUnit unit, @Out CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException, OutParamStateException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit, CancelCondition)");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit, @Out CancelCondition condition) throws TimeoutException, CancelledException, OutParamStateException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final boolean isCancellationExpired() {
		return true;
	}
	
	@Override
	public final TaskOfBool onCompletion(Consumer<Task> action) {
		Objects.requireNonNull(action, "'action' for onCompletion must not be null");
		action.accept(this);
		return this;
	}

	@Override
	public final TaskOfBool onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCompletionAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCompletionAsync must not be null");
		executor.submit(() -> action.accept(this));
		return this;
	}
	
	
	@Internal
	static class CancelledTaskOfBool extends DoneTaskOfBool {

		private final CancelledException exception;
		
		CancelledTaskOfBool(CancelledException exception) {
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
		public TaskOfBool checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOfBool checkFailure(Class<E> expectedType) throws E, ClassCastException {
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
		public TaskOfBool checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOfBool onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOfBool onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			executor.submit(() -> action.accept(exception));
			return this;
		}

		@Override
		public TaskOfBool onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOfBool onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfBool onSuccess(BooleanConsumer action) {
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(BooleanConsumer action, ExecutorService executor) {
			return this;
		}

		@Override
		public OptionalBoolean getFinishedResult() {
			return null;
		}

		@Override
		public boolean getResult() {
			return false;
		}
	}
	
	@Internal
	static class SuccessfulTaskOfBool extends DoneTaskOfBool {
		static final SuccessfulTaskOfBool FALSE_INSTANCE = new SuccessfulTaskOfBool(false);
		static final SuccessfulTaskOfBool TRUE_INSTANCE  = new SuccessfulTaskOfBool(true);
		
		private final boolean value;
		
		private SuccessfulTaskOfBool(boolean value) {
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
		public TaskOfBool checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOfBool checkFailure(Class<E> expectedType) throws E {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return this;
		}

		@Override
		public TaskOfBool checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOfBool onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			return this;
		}

		@Override
		public TaskOfBool onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
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
		public TaskOfBool onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			action.run();
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			executor.submit(action);
			return this;
		}

		@Override
		public TaskOfBool onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOfBool onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfBool onSuccess(BooleanConsumer action) {
			action.accept(value);
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(BooleanConsumer action, ExecutorService executor) {
			executor.submit(() -> action.accept(value));
			return this;
		}

		@Override
		public OptionalBoolean getFinishedResult() {
			return OptionalBoolean.of(value);
		}

		@Override
		public boolean getResult() {
			return value;
		}
	}
	
	@Internal
	static class FailedTaskOfBool extends DoneTaskOfBool {

		private final Throwable exception;
		private final AtomicBoolean isConsumed;

		FailedTaskOfBool(Throwable exception) {
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
		public TaskOfBool checkFailure() throws Throwable {
			if(isConsumed.compareAndSet(false, true)) throw exception;
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> TaskOfBool checkFailure(Class<E> expectedType) throws E {
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
		public TaskOfBool checkSuccess() throws TaskFailureException {
			if(isConsumed.compareAndSet(false, true)) {
				throw new TaskFailureException(exception);
			}
			return this;
		}

		@Override
		public TaskOfBool onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOfBool onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOfBool onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfBool onSuccess(BooleanConsumer action) {
			return this;
		}

		@Override
		public TaskOfBool onSuccessAsync(BooleanConsumer action, ExecutorService executor) {
			return this;
		}

		@Override
		public OptionalBoolean getFinishedResult() {
			return null;
		}

		@Override
		public boolean getResult() {
			return false;
		}
	}
}
