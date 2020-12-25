package dev.lb.simplebase.util.task;

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Out;
import dev.lb.simplebase.util.function.BooleanConsumer;
import dev.lb.simplebase.util.value.OptionalBoolean;

abstract class BlockingTaskOfBool implements TaskOfBool {

	protected final Awaiter awaiter;
	protected final SubscriptionHandler<CancelledException> onCancelled;
	protected final SubscriptionHandler<Boolean> onSuccess;
	protected final SubscriptionHandler<Throwable> onFailure;
	protected final SubscriptionHandler<Task> onCompletion;
	
	protected BlockingTaskOfBool() {
		this.awaiter = new Awaiter();
		this.onCancelled = new SubscriptionHandler<>();
		this.onSuccess = new SubscriptionHandler<>();
		this.onFailure = new SubscriptionHandler<>();
		this.onCompletion = new SubscriptionHandler<>();
	}
	
	@Override
	public TaskOfBool await() throws InterruptedException {
		awaiter.await(Awaiter.MASTER_PERMIT);
		return this;
	}

	@Override
	public TaskOfBool awaitUninterruptibly() {
		awaiter.awaitUninterruptibly(Awaiter.MASTER_PERMIT);
		return this;
	}
	
	@Override
	public TaskOfBool await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		awaiter.await(Awaiter.MASTER_PERMIT, timeout, unit);
		return this;
	}

	@Override
	public TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		awaiter.awaitUninterruptibly(Awaiter.MASTER_PERMIT, timeout, unit);
		return this;
	}

	@Override
	public TaskOfBool await(@Out CancelCondition condition) throws InterruptedException, CancelledException, OutParamStateException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");

		if(!condition.setupActionWithoutContext(() -> awaiter.signalAll(condition))) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		
		if(awaiter.await(condition) == condition) { //If cancelled (and not completed normally)
			throw condition.createCancelledException(); //Throw the cancellation cause
		}

		return this;
	}

	@Override
	public TaskOfBool awaitUninterruptibly(@Out CancelCondition condition) throws CancelledException, OutParamStateException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");

		if(!condition.setupActionWithoutContext(() -> awaiter.signalAll(condition))) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		
		if(awaiter.awaitUninterruptibly(condition) == condition) { //If cancelled (and not completed normally)
			throw condition.createCancelledException(); //Throw the cancellation cause
		}
		return this;
	}

	@Override
	public TaskOfBool await(long timeout, TimeUnit unit, @Out CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException, OutParamStateException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		if(!condition.setupActionWithoutContext(() -> awaiter.signalAll(condition))) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		
		if(awaiter.await(condition, timeout, unit) == condition) { //If cancelled (and not completed normally)
			throw condition.createCancelledException(); //Throw the cancellation cause
		}
		return this;
	}

	@Override
	public TaskOfBool awaitUninterruptibly(long timeout, TimeUnit unit, @Out CancelCondition condition) throws TimeoutException, CancelledException, OutParamStateException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		if(!condition.setupActionWithoutContext(() -> awaiter.signalAll(condition))) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		}
		
		if(awaiter.awaitUninterruptibly(condition, timeout, unit) == condition) { //If cancelled (and not completed normally)
			throw condition.createCancelledException(); //Throw the cancellation cause
		}
		return this;
	}
	

	@Override
	public TaskOfBool onCancelled(Consumer<CancelledException> action) {
		onCancelled.subscribe(action);
		return this;
	}

	@Override
	public TaskOfBool onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
		onCancelled.subscribeAsync(action, executor);
		return this;
	}

	@Override
	public TaskOfBool onSuccess(Runnable action) {
		onSuccess.subscribeRunnable(action);
		return this;
	}

	@Override
	public TaskOfBool onSuccessAsync(Runnable action, ExecutorService executor) {
		onSuccess.subscribeRunnableAsync(action, executor);
		return this;
	}

	@Override
	public TaskOfBool onFailure(Consumer<Throwable> action) {
		onFailure.subscribe(action);
		return this;
	}

	@Override
	public TaskOfBool onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
		onFailure.subscribeAsync(action, executor);
		return this;
	}

	@Override
	public TaskOfBool onCompletion(Consumer<Task> action) {
		onCompletion.subscribe(action);
		return this;
	}

	@Override
	public TaskOfBool onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		onCompletion.subscribeAsync(action, executor);
		return this;
	}
	

	@Override
	public TaskOfBool onSuccess(BooleanConsumer action) {
		onSuccess.subscribe(action.boxed());
		return this;
	}

	@Override
	public TaskOfBool onSuccessAsync(BooleanConsumer action, ExecutorService executor) {
		onSuccess.subscribeAsync(action.boxed(), executor);
		return this;
	}
	
	@Internal
	static class ConditionWaiterTaskOfBool extends BlockingTaskOfBool {

		private volatile CancelledException taskCancellationCause;
		private volatile Throwable thrownException;
		private volatile boolean result;
		private final AtomicBoolean consumed;
		private final AtomicInteger state;
		/*
		 * Flags:
		 * 1 -> Member access valid
		 * 2 -> Cancelled
		 * 4 -> Success
		 * 8 -> Failure
		 */
		private static final int WAITING = 1; //valid
		private static final int CANCELLING = 2; //Cancelled, not valid
		private static final int CANCELLED = 3; //Cancelled and valid
		private static final int SUCCEEDING = 4; //Success and not valid
		private static final int SUCCESS = 5; //Success and valid
		private static final int FAILING = 8; //Failed and not valid
		private static final int FAILED = 9; //Failed and valid
		
		private static final int VALID_MASK = 1;
		private static final int CANCEL_MASK = 2;
		private static final int SUCCESS_MASK = 4;
		private static final int FAILED_MASK = 8;
		
		private static final State[] STATES = {null, State.RUNNING, State.CANCELLED, State.CANCELLED, State.SUCCESS, State.SUCCESS,
				null, null, State.FAILED, State.FAILED};
		
		ConditionWaiterTaskOfBool(TaskCompleterOfBool source) {
			super();
			setupSource(source);
			
			this.state = new AtomicInteger(WAITING);
			this.consumed = new AtomicBoolean(false);
			
			this.taskCancellationCause = null;
			this.thrownException = null;
			this.result = false;
		}
		
		private void setupSource(TaskCompleterOfBool tcs) throws IllegalArgumentException {
			tcs.setup(this::succeed, this::fail, this::getCancellationException);
		}

		private boolean succeed(boolean value) throws CancelledException {
			//If not waiting, someone is currently completing or completed already
			if(!state.compareAndSet(WAITING, SUCCEEDING)) {
				if((state.get() & CANCEL_MASK) != 0) { //Cancelling or cancelled
					while(state.get() != CANCELLED) { //Wait for a stable state
						Thread.onSpinWait();
					}
					throw taskCancellationCause;
				}
				//Already succeeded/failed
				return false;
			}
			
			this.result = value;
			awaiter.signalAll(Awaiter.MASTER_PERMIT);

			//Now the threads are awake and the cause is initialized: no longer unstable
			if(!state.compareAndSet(SUCCEEDING, SUCCESS)) {
				throw new ConcurrentModificationException("ConditionWaiterTask SUCCEEDING state modified by concurrent thread");
			}
			
			onSuccess.execute(() -> null);
			onCompletion.execute(() -> this);
			
			return true;
		}
		
		private boolean fail(Throwable throwable) throws CancelledException {
			//If not waiting, someone is currently completing or completed already
			if(!state.compareAndSet(WAITING, FAILING)) {
				if((state.get() & CANCEL_MASK) != 0) { //Cancelling or cancelled
					while(state.get() != CANCELLED) { //Wait for a stable state
						Thread.onSpinWait();
					}
					throw taskCancellationCause;
				}
				//Already succeeded/failed
				return false;
			}

			this.thrownException = throwable;
			awaiter.signalAll(Awaiter.MASTER_PERMIT);
			
			//Now the threads are awake and the cause is initialized: no longer unstable
			if(!state.compareAndSet(FAILING, FAILED)) {
				throw new ConcurrentModificationException("ConditionWaiterTask FAILING state modified by concurrent thread");
			}
			
			onFailure.execute(() -> throwable);
			onCompletion.execute(() -> this);
			
			return true;
		}
		
		@Override
		public boolean cancel(Object exceptionPayload) {
			//If not waiting, someone is currently completing or completed already
			if(!state.compareAndSet(WAITING, CANCELLING)) {
				return false;
			}

			//Here: Switched WAITING -> CANCELLING
			//Create the exception: 
			this.taskCancellationCause = new CancelledException("Blocking task cancelled", exceptionPayload);
			awaiter.signalAll(Awaiter.MASTER_PERMIT);
			
			//Now the threads are awake and the cause is initialized: no longer unstable
			if(!state.compareAndSet(CANCELLING, CANCELLED)) {
				throw new ConcurrentModificationException("ConditionWaiterTask CANCELLING state modified by concurrent thread");
			}
			
			//Run the handlers afterwards, with a non-broken state
			onCancelled.execute(() -> taskCancellationCause);
			onCompletion.execute(() -> this);
			
			return true;
		}

		@Override
		public boolean isCancellationExpired() {
			return state.get() != WAITING; //Only one who can be cancelled
		}

		@Override
		public boolean isDone() {
			return state.get() != WAITING; //Is done or currently being completed
		}

		@Override
		public boolean isRunning() {
			return state.get() == WAITING; //All others are done, maintain consistency here
		}

		@Override
		public boolean isCancelled() {
			return (state.get() & CANCEL_MASK) != 0; //Cancelled or being cancelled
		}

		@Override
		public boolean isSuccessful() {
			return (state.get() & SUCCESS_MASK) != 0;
		}

		@Override
		public boolean isFailed() {
			return (state.get() & FAILED_MASK) != 0;
		}

		@Override
		public boolean isPrevented() {
			return false; //Always already started
		}

		@Override
		public boolean isSynchronous() {
			return false;
		}

		@Override
		public State getState() {
			return STATES[state.get()];
		}

		@Override
		public TaskOfBool checkFailure() throws Throwable {
			//Not failed at all
			if((state.get() & FAILED_MASK) != 0) {
				return this;
			}
			//Spin-wait until stable
			while((state.get() & VALID_MASK) == 0) {
				Thread.onSpinWait();
			}
			//Consume and throw
			if(consumed.compareAndSet(false, true)) {
				throw  thrownException;
			}

			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> TaskOfBool checkFailure(Class<E> expectedType) throws E {
			//Not failed at all
			if((state.get() & FAILED_MASK) != 0) {
				return this;
			}
			//Spin-wait until stable
			while((state.get() & VALID_MASK) == 0) {
				Thread.onSpinWait();
			}
			//Consume and throw
			if(expectedType.isInstance(thrownException)) {
				if(consumed.compareAndSet(false, true)) {
					throw (E) thrownException;
				}
			}
			return this;
		}

		@Override
		public Throwable getFailure() {
			return thrownException;
		}

		@Override
		public boolean hasUnconsumedException() {
			return !consumed.get();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException {
			return (E) thrownException;
		}

		@Override
		public TaskOfBool checkSuccess() throws TaskFailureException {
			//Not failed at all
			if((state.get() & FAILED_MASK) != 0) {
				return this;
			}
			//Spin-wait until stable
			while((state.get() & VALID_MASK) == 0) {
				Thread.onSpinWait();
			}
			//Consume and throw
			if(consumed.compareAndSet(false, true)) {
				throw new TaskFailureException(thrownException);
			}

			return this;
		}

		@Override
		public boolean cancelIfRunning(Object exceptionPayload) {
			return cancel(exceptionPayload); //Always running or done, which is this behavior
		}

		@Override
		public boolean cancelIfNotStarted(Object exceptionPayload) {
			return false; //Always already started
		}

		@Override
		public CancelledException getCancellationException() {
			return taskCancellationCause; //Just read - it's either there or not
		}

		@Override
		public OptionalBoolean getFinishedResult() {
			if((state.get() & SUCCESS_MASK) != 0) {
				return null;
			}
			//Spin-wait until stable
			while((state.get() & VALID_MASK) == 0) {
				Thread.onSpinWait();
			}
			return OptionalBoolean.of(result);
		}

		@Override
		public boolean getResult() {
			return result;
		}

	}
}

