package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Out;

@Internal
abstract class DoneTaskOfInt implements TaskOfInt {
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
	public final TaskOfInt await() throws InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await()");
		return this;
	}

	@Override
	public final TaskOfInt awaitUninterruptibly() {
		return this;
	}

	@Override
	public final TaskOfInt await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit)");
		return this;
	}

	@Override
	public final TaskOfInt awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		return this;
	}

	@Override
	public final TaskOfInt await(@Out CancelCondition condition) throws InterruptedException, CancelledException, OutParamStateException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(CancelCondition)");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfInt awaitUninterruptibly(@Out CancelCondition condition) throws CancelledException, OutParamStateException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfInt await(long timeout, TimeUnit unit, @Out CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException, OutParamStateException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit, CancelCondition)");
		if(!condition.setupActionWithoutContext(ex -> false)) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		return this;
	}

	@Override
	public final TaskOfInt awaitUninterruptibly(long timeout, TimeUnit unit, @Out CancelCondition condition) throws TimeoutException, CancelledException, OutParamStateException {
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
	public final TaskOfInt onCompletion(Consumer<Task> action) {
		Objects.requireNonNull(action, "'action' for onCompletion must not be null");
		action.accept(this);
		return this;
	}

	@Override
	public final TaskOfInt onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCompletionAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCompletionAsync must not be null");
		executor.submit(() -> action.accept(this));
		return this;
	}
	
	
	@Internal
	static class CancelledTaskOfInt extends DoneTaskOfInt {

		private final CancelledException exception;
		
		CancelledTaskOfInt(CancelledException exception) {
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
		public TaskOfInt checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOfInt checkFailure(Class<E> expectedType) throws E, ClassCastException {
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
		public TaskOfInt checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOfInt onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOfInt onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			executor.submit(() -> action.accept(exception));
			return this;
		}

		@Override
		public TaskOfInt onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOfInt onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfInt onSuccess(IntConsumer action) {
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(IntConsumer action, ExecutorService executor) {
			return this;
		}

		@Override
		public OptionalInt getFinishedResult() {
			return null;
		}

		@Override
		public int getResult() {
			return 0;
		}
	}
	
	@Internal
	static class SuccessfulTaskOfInt extends DoneTaskOfInt {
		
		private final int value;
		
		SuccessfulTaskOfInt(int value) {
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
		public TaskOfInt checkFailure() throws Throwable {
			return this;
		}

		@Override
		public <E extends Throwable> TaskOfInt checkFailure(Class<E> expectedType) throws E {
			Objects.requireNonNull(expectedType, "expected exception type must not be null");
			return this;
		}

		@Override
		public TaskOfInt checkSuccess() throws TaskFailureException {
			return this;
		}

		@Override
		public TaskOfInt onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelled must not be null");
			return this;
		}

		@Override
		public TaskOfInt onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
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
		public TaskOfInt onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			action.run();
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			executor.submit(action);
			return this;
		}

		@Override
		public TaskOfInt onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			return this;
		}

		@Override
		public TaskOfInt onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfInt onSuccess(IntConsumer action) {
			action.accept(value);
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(IntConsumer action, ExecutorService executor) {
			executor.submit(() -> action.accept(value));
			return this;
		}

		@Override
		public OptionalInt getFinishedResult() {
			return OptionalInt.of(value);
		}

		@Override
		public int getResult() {
			return value;
		}
	}
	
	@Internal
	static class FailedTaskOfInt extends DoneTaskOfInt {

		private final Throwable exception;
		private final AtomicBoolean isConsumed;

		FailedTaskOfInt(Throwable exception) {
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
		public TaskOfInt checkFailure() throws Throwable {
			if(isConsumed.compareAndSet(false, true)) throw exception;
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> TaskOfInt checkFailure(Class<E> expectedType) throws E {
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
		public TaskOfInt checkSuccess() throws TaskFailureException {
			if(isConsumed.compareAndSet(false, true)) {
				throw new TaskFailureException(exception);
			}
			return this;
		}

		@Override
		public TaskOfInt onCancelled(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onCancelledAsync(Consumer<CancelledException> action) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onSuccess(Runnable action) {
			Objects.requireNonNull(action, "'action' for onSuccess must not be null");
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(Runnable action, ExecutorService executor) {
			Objects.requireNonNull(action, "'action' for onSuccessAsync must not be null");
			Objects.requireNonNull(executor, "'executor' for onSuccessAsync must not be null");
			return this;
		}

		@Override
		public TaskOfInt onFailure(Consumer<Throwable> action) {
			Objects.requireNonNull(action, "'action' for onFailure must not be null");
			action.accept(exception);
			return this;
		}

		@Override
		public TaskOfInt onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
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
		public TaskOfInt onSuccess(IntConsumer action) {
			return this;
		}

		@Override
		public TaskOfInt onSuccessAsync(IntConsumer action, ExecutorService executor) {
			return this;
		}

		@Override
		public OptionalInt getFinishedResult() {
			return null;
		}

		@Override
		public int getResult() {
			return 0;
		}
	}
}
