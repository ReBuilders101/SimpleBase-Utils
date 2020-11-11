package dev.lb.simplebase.util.task;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dev.lb.simplebase.util.annotation.Threadsafe;

/**
 * An awaiter will allow threads to wait until the awaiter is signalled by another thread.
 * <p>
 * While mostly identical to a {@link Condition}, an {@link Awaiter} stores an internal set of
 * permits, that can be made available before any threads begin waiting. The permits are one-way: If they have been
 * granted, they will remain forever
 * </p>
 */
@Threadsafe
public final class Awaiter {
	
	/**
	 * The permit that releases all awaiting threads, regardless of the permit they used.
	 * Can be awaited to not wake up on any special permits at all
	 */
	public static final Object MASTER_PERMIT = null;
	
	private final Lock conditionLock;
	private final Condition condition;
	private final Set<Object> availablePermits;
	
	/**
	 * Creates a new {@link Awaiter} that maintains its own {@link Lock} and {@link Condition}.
	 * The permit will initially not be availabe.
	 */
	public Awaiter() {
		this.conditionLock = new ReentrantLock();
		this.condition = conditionLock.newCondition();
		this.availablePermits = new HashSet<>();
	}
	
	/**
	 * Waits for the permit to become available. If it is available when the method is called, it
	 * returns immediately.
	 * @param permit The permit key to wait for
	 * @throws InterruptedException When the waiting thread was interrupted
	 * @return The permit that signalled the awaiter
	 */
	public Object await(Object permit) throws InterruptedException {
		//Don't wait when the permit is available
		if(availablePermits.contains(MASTER_PERMIT)) {
			return MASTER_PERMIT;
		} else if(availablePermits.contains(permit)) {
			return permit;
		} else {
			conditionLock.lock();
			try {
				//Check again in case another thread gave the permit before we could start waiting
				//Spin until we wake up with permit to avoid spurious wakeups
				while(true) {
					if(availablePermits.contains(MASTER_PERMIT)) {
						return MASTER_PERMIT;
					} else if(availablePermits.contains(permit)) {
						return permit;
					} else {
						condition.await();
					}
				}
			} finally {
				conditionLock.unlock();
			}
		}
	}
	
	/**
	 * Waits for the permit to become available. If it is available when the method is called, it
	 * returns immediately. If the thread is interrupted while waiting, it will continue to wait and the
	 * interrupt flag will be set on this thread after the method returns.
	 * @param permit The permit key to wait for
	 * @return The permit that signalled the awaiter
	 */
	public Object awaitUninterruptibly(Object permit) {
		if(availablePermits.contains(MASTER_PERMIT)) {
			return MASTER_PERMIT;
		} else if(availablePermits.contains(permit)) {
			return permit;
		} else {
			conditionLock.lock();
			try {
				//Check again in case another thread gave the permit before we could start waiting
				//Spin until we wake up with permit to avoid spurious wakeups
				while(true) {
					if(availablePermits.contains(MASTER_PERMIT)) {
						return MASTER_PERMIT;
					} else if(availablePermits.contains(permit)) {
						return permit;
					} else {
						condition.awaitUninterruptibly();
					}
				}
			} finally {
				conditionLock.unlock();
			}
		}
	}
	
	/**
	 * Waits for the permit to become available or the the timeout to elapse. If the permit is available when the method is called, it
	 * returns immediately.
	 * @param permit The permit key to wait for
	 * @param timeout The maximum time to wait
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return The permit that signalled the awaiter
	 * @throws InterruptedException When the waiting thread was interrupted
	 * @throws TimeoutException When the timeout expires before being signalled
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public Object await(Object permit, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		
		if(availablePermits.contains(MASTER_PERMIT)) {
			return MASTER_PERMIT;
		} else if(availablePermits.contains(permit)) {
			return permit;
		} else {
			conditionLock.lock();
			try {
				//Check again in case another thread gave the permit before we could start waiting
				//Spin until we wake up with permit to avoid spurious wakeups
				while(true) {
					if(availablePermits.contains(MASTER_PERMIT)) {
						return MASTER_PERMIT;
					} else if(availablePermits.contains(permit)) {
						return permit;
					} else {
						if(!condition.await(timeout, unit)) {
							throw new TimeoutException("Awaiter: timeout elapsed before being signalled");
						}
					}
				}
			} finally {
				conditionLock.unlock();
			}
		}
	}
	
	/**
	 * Waits for the permit to become available or the the timeout to elapse. If the permit is available when the method is called, it
	 * returns immediately. If the thread is interrupted while waiting, it will continue to wait and the
	 * interrupt flag will be set on this thread after the method returns.
	 * @param permit The permit key to wait for
	 * @param timeout The maximum time to wait
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return The permit that signalled the awaiter
	 * @throws TimeoutException When the timeout expires before being signalled
	 * @throws NullPointerException When {@code unit} is {@code null}
	 */
	public Object awaitUninterruptibly(Object permit, long timeout, TimeUnit unit) throws TimeoutException {
		
		if(availablePermits.contains(MASTER_PERMIT)) {
			return MASTER_PERMIT;
		} else if(availablePermits.contains(permit)) {
			return permit;
		} else {
			conditionLock.lock();
			try {
				//Check again in case another thread gave the permit before we could start waiting
				//Spin until we wake up with permit to avoid spurious wakeups
				while(true) {
					if(availablePermits.contains(MASTER_PERMIT)) {
						return permit;
					} else if(availablePermits.contains(permit)) {
						return MASTER_PERMIT;
					} else {
						if(!awaitUniterruptiblyImpl(timeout, unit)) {
							throw new TimeoutException("Awaiter: timeout elapsed before being signalled");
						}
					}
				}
			} finally {
				conditionLock.unlock();
			}
		}
	}
	
	/**
	 * Makes the permit available and wakes up all threads waiting for that permit,
	 * or all threads if it is the {@link #MASTER_PERMIT}.
	 * @param permit The permit key to wait for
	 */
	public void signalAll(Object permit) {
		if(availablePermits.contains(permit)) {
			return;
		} else {
			conditionLock.lock();
			try {
				//Check again for permit
				if(!availablePermits.contains(permit)) {
					//Give permit and signal
					availablePermits.add(permit);
					condition.signalAll();
				}
			} finally {
				conditionLock.unlock();
			}
		}
	}
	
	private boolean awaitUniterruptiblyImpl(long timeout, TimeUnit unit) {
		boolean interrupted = false;
		long remainingNanos = unit.toNanos(timeout);
		long deadlineNanos = System.nanoTime();
		
		//Repeat-wait until we return a result
		while(true) {
			try {
				boolean success = condition.await(remainingNanos, TimeUnit.NANOSECONDS);
				//If we arrive here, no interruptions happened
				if(interrupted) Thread.currentThread().interrupt(); //reset flag
				return success; //Stop waiting
			} catch (InterruptedException e) {
				interrupted = true;
				//Calculate how much more we have to wait
				remainingNanos = deadlineNanos - System.nanoTime();
				//Then repeat loop to wait the remaining time
			}
		}
	}
	
}
