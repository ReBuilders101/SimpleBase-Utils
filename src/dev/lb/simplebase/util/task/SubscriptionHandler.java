package dev.lb.simplebase.util.task;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Threadsafe;


/**
 * A {@link SubscriptionHandler} manages a threadsafe list of subscriber actions
 * that are all called when the {@link #execute(Supplier)} method is called.
 * @param <Context> The type of context object that the subscriber actions will receive
 */
@Threadsafe //And lock-free too
public class SubscriptionHandler<Context> {

	private final Queue<Consumer<Context>> actions; //Threadsafe access guareded by state
	private volatile Context context; //Threadsafe access guareded by state
	private final AtomicInteger state;
	
	private static final int COLLECTING = 0;
	private static final int ADDING = 1;
	private static final int RUNNING = 2;
	private static final int EXPIRED = 3;
	private static final int ALREADY_EXECUTED_FLAG = 2;
	
	/**
	 * Creates a new {@link SubscriptionHandler} with an empty subscriber list.
	 */
	public SubscriptionHandler() {
		this.actions = new LinkedList<>();
		this.state = new AtomicInteger(COLLECTING);
		this.context = null;
	}
	
	/**
	 * Executes all subscribed actions in the order that they were subscribed.
	 * Afterwards, {@link #hasBeenExecuted()} will return {@code true}.
	 * @param executionContext A {@link Supplier} that produces the context object. The supplier will only be called once.
	 * @return {@code true} if all actions were executed, {@code false} if {@link #hasBeenExecuted()} was true and the handlers have already run.
	 */
	public boolean execute(Supplier<Context> executionContext) {
		Objects.requireNonNull(executionContext, "execution context supplier must not be null");
		//Spin-wait for COLLECTING
		while(!state.compareAndSet(COLLECTING, RUNNING)) {
			//Here we are RUNNING, ADDING or EXPIRED. RUNNING and ADDING will go away if we spin some more.
			if(state.get() == EXPIRED) { //IF RUNNING proceeded to EXPIRED while spinning, we cannot cancel again
				return false;
			} else {
				//Else state is ADDING or RUNNING, spin until adding/draining is done
				Thread.onSpinWait();
			}
		}
		//State here is RUNNING because the CAS in the previous spin-wait succeeded
		//Create the exception and store it in the field
		this.context = executionContext.get();
		//Run all queued actions synchronously with the created exception
		for(Consumer<Context> exc : actions) {
			exc.accept(context);
		}
		//Switch state to EXPIRED and check for desyncs
		if(!state.compareAndSet(RUNNING, EXPIRED)) {
			throw new ConcurrentModificationException("CancelConditionImpl RUNNING state modified by concurrent thread");
		}
		return true;
	}
	
	/**
	 * Will return {@code true} if {@link #execute(Supplier)} has been called before and the subscriber 
	 * actions have already run, {@code false} if they can still be executed.
	 * @return {@code true} if the handlers have already run, {@code false} otherwise
	 */
	public boolean hasBeenExecuted() {
		return (state.get() & ALREADY_EXECUTED_FLAG) != 0;
	}
	
	/**
	 * The current context for the subscribed methods. The context may be {@code null} before {@link #execute(Supplier)} is called.
	 * @return The current context object.
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Adds an action to the subscriber list if possible, or runs the action immediately.
	 * <p>
	 * If this handler has already been executed ({@link #hasBeenExecuted()}), the action will run immediately
	 * on the calling thread. Otherwise, then action is added to the subscriber list and will run when {@link #execute(Supplier)} is called.
	 * </p>
	 * @param action The {@link Consumer} that will receive the execution context when ran
	 */
	public void subscribe(Consumer<Context> action) {
		Objects.requireNonNull(action, "'action' for onCancelled must not be null");
		
		//Spin-wait until we are collecting, so we can switch into adding mode
		while(!state.compareAndSet(COLLECTING, ADDING)) {
			//Except if it is already cancelled, then we should run synchronously now
			if((state.get() & ALREADY_EXECUTED_FLAG) != 0) {
				//Spin-wait (again) until draining of queued actions is done
				while(state.get() == RUNNING) {
					Thread.onSpinWait();
				}
				//Run synchrounously (state here will be EXPIRED
				action.accept(context);
				//Done here, prevent spinning any more
				return;
			} else {
				Thread.onSpinWait();
			}
		}
		//State here is ADDING because the CAS in the previous spin wait succeeded
		actions.add(action); //Add our element
		//Reset state to COLLECTING and check for any desyncs
		if(!state.compareAndSet(ADDING, COLLECTING)) {
			throw new ConcurrentModificationException("CancelConditionImpl ADDING state modified by concurrent thread");
		}
		return;
	}
	
	void subscribeRunnable(Runnable action) {
		Objects.requireNonNull(action, "'action' for onCancelled must not be null");

		//Spin-wait until we are collecting, so we can switch into adding mode
		while(!state.compareAndSet(COLLECTING, ADDING)) {
			//Except if it is already cancelled, then we should run synchronously now
			if((state.get() & ALREADY_EXECUTED_FLAG) != 0) {
				//Spin-wait (again) until draining of queued actions is done
				while(state.get() == RUNNING) {
					Thread.onSpinWait();
				}
				//Run synchrounously (state here will be EXPIRED
				action.run();
				//Done here, prevent spinning any more
				return;
			} else {
				Thread.onSpinWait();
			}
		}
		//State here is ADDING because the CAS in the previous spin wait succeeded
		actions.add((ex) -> action.run()); //Add our element
		//Reset state to COLLECTING and check for any desyncs
		if(!state.compareAndSet(ADDING, COLLECTING)) {
			throw new ConcurrentModificationException("CancelConditionImpl ADDING state modified by concurrent thread");
		}
		return;
	}
	
	void subscribeRunnableAsync(Runnable action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
		subscribeRunnable(() -> executor.submit(action));
	}

	/**
	 * Adds an action to the subscriber list if possible, or runs the action immediately.
	 * <p>
	 * If this handler has already been executed ({@link #hasBeenExecuted()}), the action will be immediately 
	 * submitted to the executor. Otherwise, then action is added to the subscriber list and will be submitted
	 * to the executor when {@link #execute(Supplier)} is called.
	 * </p>
	 * @param action The {@link Consumer} that will receive the execution context when ran
	 * @param executor The {@link ExecutorService} that will execute the action
	 */
	public void subscribeAsync(Consumer<Context> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCancelledAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCancelledAsync must not be null");
		subscribe((ex) -> executor.submit(() -> action.accept(ex)));
	}

	/**
	 * A {@link CancelCondition} that can be associated with any action
	 */
	@Internal
	static final class StandaloneCancelCondition extends SubscriptionHandler<CancelledException> implements CancelCondition {

		@Override
		public boolean cancel(Object exceptionPayload) {
			return execute(() -> new CancelledException(exceptionPayload));
		}

		@Override
		public boolean isCancellationExpired() {
			return hasBeenExecuted();
		}

		@Override
		public CancelCondition onCancelled(Consumer<CancelledException> action) {
			subscribe(action); //Subscribe checks for null
			return this;
		}

		@Override
		public CancelCondition onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
			subscribeAsync(action, executor); //SubscribeAsync checks for null
			return this;
		}

		@Override
		public CancelledException getCancellationException() {
			return getContext();
		}

		@Override
		public boolean isCancelled() {
			return hasBeenExecuted();
		}
		
	}
	
}
