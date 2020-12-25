package dev.lb.simplebase.util.timer;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dev.lb.simplebase.util.OutParamStateException;
import dev.lb.simplebase.util.annotation.Out;
import dev.lb.simplebase.util.annotation.StaticType;
import dev.lb.simplebase.util.task.CancelCondition;
import dev.lb.simplebase.util.value.Lazy;

/**
 * Global multi-thread timer task scheduler
 */
@StaticType
public final class GlobalTimer {
	
	private static final Lazy<ScheduledExecutorService> service = Lazy.of(() -> Executors.newScheduledThreadPool(1));
	
	private GlobalTimer() {}
	
	/**
	 * Schedules a task to run a single time in the future.
	 * @param task The {@link Runnable} that will run
	 * @param timeout The time to wait before running
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A {@link Delayed} that reports how much time is left until the action runs
	 * @throws RejectedExecutionException When the task could not be submitted to the backing {@link ExecutorService}
	 * @throws NullPointerException When {@code task} or {@code unit} is {@code null}
	 */
	public static Delayed scheduleOnce(Runnable task, long timeout, TimeUnit unit) {
		return service.get().schedule(task, timeout, unit);
	}
	
	/**
	 * Schedules a task to run a single time in the future.
	 * @param task The {@link Runnable} that will run
	 * @param condition <i>&#064;Out</i> A {@link CancelCondition} that can be cancelled to prevent execution
	 * @param timeout The time to wait before running
	 * @param unit The {@link TimeUnit} for the timeout
	 * @return A {@link Delayed} that reports how much time is left until the action runs
	 * @throws OutParamStateException When the {@code condition} has already been assoicated with a different action
	 * @throws RejectedExecutionException When the task could not be submitted to the backing {@link ExecutorService}
	 * @throws NullPointerException When {@code task} or {@code unit} is {@code null}
	 */
	public static Delayed scheduleOnce(Runnable task, @Out CancelCondition condition, long timeout, TimeUnit unit) throws OutParamStateException {
		var future = condition.setupActionWithContext(() ->  service.get().schedule(task, timeout, unit), f -> f.cancel(false));
		if(future == null) {
			throw new OutParamStateException("Out parameter 'condition' was already associated with an action");
		} else {
			return future;
		}
	}
	
	/**
	 * Shuts down the backing executor. Attempts to schedule new tasks after this method
	 * has been called will result in a {@link RejectedExecutionException}.
	 * @see ExecutorService#shutdown()
	 */
	public static void shutdown() {
		service.get().shutdown();
	}
	
	/**
	 * Shuts down the backing executor service and waits until all running tasks have completed
	 * @param timeout The time to wait for running tasks to complete
	 * @param unit The {@link TimeUnit} for the timeout
	 * @throws InterruptedException When the calling thread is interrupted while waiting
	 * @throws TimeoutException When the timeout elapses before all tasks complete
	 * @see ExecutorService#shutdown()
	 * @see ExecutorService#awaitTermination(long, TimeUnit)
	 */
	public static void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		service.get().shutdown();
		if(!service.get().awaitTermination(timeout, unit)) {
			throw new TimeoutException();
		}
	}
	
	/**
	 * Attempts to force-shutdown all running tasks by interrupting their threads.
	 * Some tasks might be stopped in an incomplete or inconsistent state when this method
	 * is used. Tasks that ignore interrupts might ignore this method call entirely.
	 * @see ExecutorService#shutdownNow()
	 */
	public static void forceShutdown() {
		service.get().shutdownNow();
	}
	
	/**
	 * If {@code true}, a task can be submitted using one of the {@code schedule...} methods of this class.
	 * If {@code false}, any attempts to schedule a new task will result in a {@link RejectedExecutionException}.
	 * @return {@code true} if tasks can still be scheduled, {@code false} if attempts to schedule a new task will fail
	 */
	public static boolean isAcceptingTasks() {
		return !service.get().isShutdown();
	}
}
