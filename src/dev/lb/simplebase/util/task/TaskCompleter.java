package dev.lb.simplebase.util.task;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import dev.lb.simplebase.util.annotation.Threadsafe;

/**
 * Provides methods to complete a blocking task.
 */
@Threadsafe
public final class TaskCompleter {

	private volatile BooleanSupplier successComplete;
	private volatile Predicate<Throwable> failComplete;
	
	private final AtomicInteger state;
	private static final int UNSET = 0;
	private static final int SETTING = 1;
	private static final int SET = 2;
	
	public TaskCompleter() {
		this.state = new AtomicInteger(UNSET);
		this.successComplete = null;
		this.failComplete = null;
	}
	
	public boolean signalSuccess() {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return successComplete.getAsBoolean();
		}
	}
	
	public boolean signalFailure(Throwable throwable) {
		if(state.get() == UNSET) {
			throw new IllegalStateException("TaskCompletionSource is not set up with a task");
		} else {
			while(state.get() != SET) {
				Thread.onSpinWait();
			}
			return failComplete.test(throwable);
		}
	}
	
	void setup(/*notnull*/BooleanSupplier success, /*notnull*/Predicate<Throwable> fail) {
		if(!state.compareAndSet(UNSET, SETTING)) {
			throw new IllegalArgumentException("TaskCompletionSource is already in use"); //Yes, Illegal arg not state
		}
		
		this.successComplete = success;
		this.failComplete = fail;
		
		if(!state.compareAndSet(SETTING, SET)) {
			throw new ConcurrentModificationException("TaskCompletionSource SETTING state modified by concurrent thread");
		}
	}
	
}
