package dev.lb.simplebase.util.task;

import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import dev.lb.simplebase.util.annotation.Internal;

@Internal
class ConditionWaiterTask<T> extends BlockingTask<T> {

	private volatile CancelledException taskCancellationCause;
	private volatile Throwable thrownException;
	private volatile T result;
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
	
	ConditionWaiterTask(TaskCompleterOf<T> source) {
		super();
		setupSource(source);
		
		this.state = new AtomicInteger(WAITING);
		this.consumed = new AtomicBoolean(false);
		
		this.taskCancellationCause = null;
		this.thrownException = null;
		this.result = null;
	}
	
	private void setupSource(TaskCompleterOf<T> tcs) throws IllegalArgumentException {
		tcs.setup(this::succeed, this::fail);
	}

	private boolean succeed(T value) {
		//If not waiting, someone is currently completing or completed already
		if(!state.compareAndSet(WAITING, SUCCEEDING)) {
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
	
	private boolean fail(Throwable throwable) {
		//If not waiting, someone is currently completing or completed already
		if(!state.compareAndSet(WAITING, FAILING)) {
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
		this.taskCancellationCause = new CancelledException(exceptionPayload);
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
	public TaskOf<T> checkFailure() throws Throwable {
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
	public <E extends Throwable> TaskOf<T> checkFailure(Class<E> expectedType) throws E {
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
	public TaskOf<T> checkSuccess() throws TaskFailureException {
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
	public Optional<T> getFinishedResult() {
		if((state.get() & SUCCESS_MASK) != 0) {
			return null;
		}
		//Spin-wait until stable
		while((state.get() & VALID_MASK) == 0) {
			Thread.onSpinWait();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public T getResult() {
		return result;
	}

}
