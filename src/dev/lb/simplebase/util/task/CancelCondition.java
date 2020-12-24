package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.lb.simplebase.util.ImpossibleException;
import dev.lb.simplebase.util.annotation.Internal;
import dev.lb.simplebase.util.annotation.Threadsafe;
import dev.lb.simplebase.util.value.AssignOnce;

/**
 * A {@link CancelCondition} object can be used to request cancellation of an asynchronous or blocking task.
 * Cancellation may not be supported by all types of task.
 */
@Threadsafe
public final class CancelCondition {

	private final SubscriptionHandler<CancelledException> handlers;
	private final AssignOnce<Predicate<CancelledException>> cancelAction; //Can be externally modified
	private final AssignOnce<CancelledException> exception;
	private final AtomicInteger state;
	
	private static final int IDLE = 0;
	private static final int TESTING = 1;
	private static final int RUNNING = 2;
	private static final int EXECUTED = 3;
	
	private static int EXPIRED_MASK = 2;
	
	private CancelCondition() {
		this.handlers = new SubscriptionHandler<>();
		this.cancelAction = new AssignOnce<>();
		this.exception = new AssignOnce<>();
		this.state = new AtomicInteger();
	}
	
	/**
	 * Assigns the cancel action for this {@link CancelCondition}. Used internally.
	 * @param <Context> The type of created context object
	 * @param create The context object producer
	 * @param action The method to call on the context
	 * @return The context object, or {@code null} if setup unsuccessful
	 * @throws NullPointerException When {@code create} or {@code action} is {@code null}, or {@code create} returns {@code null}
	 */
	@Internal
	public <Context> Context setupActionWithContext(Supplier<Context> create, BiPredicate<Context, CancelledException> action) throws NullPointerException {
		return cancelAction.tryAssignWithContext(create, ctx -> ex -> action.test(ctx, ex));
	}
	
	/**
	 * Assigns the cancel action for this {@link CancelCondition}. Used internally.
	 * @param function The cancellation action
	 * @return {@code true} if assigned successfully, {@code false} otherwise
	 */
	@Internal
	public boolean setupActionWithoutContext(Predicate<CancelledException> function) {
		return cancelAction.tryAssign(function);
	}
	
	/**
	 * Attempts to cancel the associated task or blocking action and runs all cancellation if successful.
	 * If cancellation succeeds, all further calls to this method will return {@code false} immediately.
	 * <p>
	 * If no action is associated with this {@link CancelCondition}, the method returns {@code false}.<br>
	 * </p>
	 * @return {@code true} if cancellation was successful, {@code false} if not
	 * @throws IllegalStateException When this CancelCondition has not ye been associated with a cancellable action.
	 */
	public boolean cancel() throws IllegalStateException {
		return cancel(null);
	}
	
	/**
	 * Attempts to cancel the associated task or blocking action and runs all cancellation handlers if successful.
	 * <p>
	 * If no action is associated with this {@link CancelCondition}, the method returns {@code false}.
	 * </p>
	 * @param exceptionPayload A nullable object that will be available on the {@link CancelledException} caused by cancelling the task
	 * @return {@code true} if cancellation was successful, {@code false} if not
	 */
	public boolean cancel(/*Nullable*/ Object exceptionPayload) {
		//If no action is present, we can't cancel anything
		if(!cancelAction.isAssigned()) return false;
		
		while(!state.compareAndSet(IDLE, TESTING)) {
			if((state.get() & EXPIRED_MASK) != 0) {
				return false; //Someone else cancelled the action successfully
			} else {
				Thread.onSpinWait();
			}
		}
		//Acquired testing state
		final var pred = cancelAction.getValue();
		final var caex = new CancelledException(exceptionPayload);
		//If cancellation was a success
		if(pred.test(caex)) {
			if(!state.compareAndSet(TESTING, RUNNING)) {
				throw new ImpossibleException("CancelCondition TESTING state modified by concurrent thread");
			}
			//Set exception and run handlers
			if(!exception.tryAssign(caex)) {
				throw new ImpossibleException("Cannot set cancel exception twice");
			}
			
			if(!handlers.execute(() -> caex)) {
				throw new ImpossibleException("Cannot run cancel handlers twice");
			}
			
			if(!state.compareAndSet(RUNNING, EXECUTED)) {
				throw new ImpossibleException("CancelCondition RUNNING state modified by concurrent thread");
			}
			return true;
		} else {
			//Not cancelled, no handlers, release state
			if(!state.compareAndSet(TESTING, IDLE)) {
				throw new ImpossibleException("CancelCondition TESTING state modified by concurrent thread");
			}
			return false;
		}
	}
	
	/**
	 * A {@link CancelledException} that was responsible for the cancellation of this condition.
	 * Will be {@code null} if {@link #isCancelled()} is {@code false}.
	 * @return The {@link CancelledException} that cancelled this condition
	 */
	public CancelledException getCancellationException() {
		return exception.getNullable();
	}
	
	/**
	 * Checks whether this condition has already been cancelled.
	 * @return {@code true} if the condition was cancelled, {@code false} otherwise
	 */
	public boolean isCancelled() {
		return (state.get() & EXPIRED_MASK) != 0;
	}
	
	/**
	 * Adds a handler that will be run when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * run immediately on the calling thread. If it is not yet cancelled, the action will run
	 * on the thread that calls the {@link #cancel()} method on this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public CancelCondition onCancelled(Consumer<CancelledException> action) {
		handlers.subscribe(action);
		return this;
	}
	/**
	 * Adds a handler that will be run with the {@link Task#defaultExecutor()} when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} is {@code null}
	 */
	public CancelCondition onCancelledAsync(Consumer<CancelledException> action) {
		return onCancelledAsync(action, Task.defaultExecutor());
	}
	/**
	 * Adds a handler that will be run with an {@link ExecutorService} when this condition is cancelled.
	 * <p>
	 * If the condition is already cancelled when calling this method, the the action will
	 * be immediately submitted to the executor. If it is not yet cancelled, the action will run
	 * be submitted to the executor when {@link #cancel()} is called for this {@link CancelCondition}.
	 * </p>
	 * @param action The {@link Consumer} that will be run when the condition is cancelled
	 * @param executor The {@link ExecutorService} that will execute the action
	 * @return This {@link CancelCondition}
	 * @throws NullPointerException When {@code action} or {@code executor} is {@code null}
	 */
	public CancelCondition onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
		handlers.subscribeAsync(action, executor);
		return this;
	}

	/**
	 * Creates a standalone {@link CancelCondition} not directly associated with any task or action.
	 * <p>
	 * The created condition can be associated with one or more blocking operations by passing it as
	 * a parameter, e.g. {@link Task#await(CancelCondition)} will associate the {@code CancelCondition}
	 * with the blocking {@code await} call (and not with the awaited task).
	 * </p><p>
	 * A {@link Task} extends {@link CancelCondition}, so it is not necessary to create a separate condition to cancel a {@code Task}.
	 * </p>
	 * @return A new {@link CancelCondition} not assicated with any action
	 */
	public static CancelCondition create() {
		return new CancelCondition();
	}
}
