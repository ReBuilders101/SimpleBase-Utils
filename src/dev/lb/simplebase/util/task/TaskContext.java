package dev.lb.simplebase.util.task;

import java.util.concurrent.TimeUnit;
import dev.lb.simplebase.util.annotation.Threadsafe;

/**
 * A {@link TaskContext} is givce to every action that
 * runs as a {@link Task}. The context can handle cancellation requests.
 */
@Threadsafe
public interface TaskContext {

	/**
	 * Returns {@code true} if cancellation of the task was requested.
	 * @return {@code true} if cancellation of the task was requested
	 */
	public boolean shouldCancel();
	
	/**
	 * Confirms that the task has done necessary cleanup and is ready to be cancelled.
	 * <p>
	 * Will thrown an error that extends the {@link Error} class (to avoid being caught by {@link Exception} or
	 * {@link RuntimeException} handlers, similar to {@link ThreadDeath}). For the cancellation to
	 * be successful, this error must not be caught by the task action and must propagate to the task runner,
	 * who will handle the exception and update the task status.
	 * </p><p>
	 * Does nothing if {@link #shouldCancel()} is false.
	 * </p>
	 */
	public void confirmCancelled(); /* Implementations should throw TaskCancellationConfirmed */
	
	/**
	 * A {@link TaskContext} is only valid while the associated {@link Task} is running.
	 * @return {@code true} if the associated task is valid
	 */
	public boolean isValid();
	
	/**
	 * A {@link TaskContext} is deferred when the associated action is not currently running, but the task is not done yet either
	 * @return {@code true} if the task is not done, but currently waiting, {@code false} otherwise
	 */
	public boolean isDeferred();
	
	/**
	 * Defers part of the {@link TaskAction} until the timeout has elapsed.
	 * <p>
	 * The {@link TaskAction} implementation that calls this method should return from the action immediately afterwards.
	 * The {@link TaskAction} that is passed as a parameter will start running after the timeout has elapsed. It will
	 * run with the {@link TaskContext} that this method is being called on.
	 * </p><p>
	 * If the associated {@link TaskAction} is being executed synchronously, this method may block
	 * the calling thread for the timeout duration and then continue with the {@code continueAction}.
	 * </p>
	 * @param timeout The timeout to wait for 
	 * @param unit The {@link TimeUnit} for the timeout
	 * @param continueAction The {@link TaskAction} that will contiue running after the timeout
	 * @param canCancel If {@code true}, an attempt to cancel the associated task while waiting will start the action before the timeout elapses
	 * @throws TaskDeferredExecption When the task associated with this context is already deferred by a previous call to this method
	 * @throws NullPointerException When {@code unit} or {@code continueAction} is {@code null}
	 */
	public void setTimeout(long timeout, TimeUnit unit, TaskAction continueAction, boolean canCancel) throws TaskDeferredExecption;
	
	/**
	 * Defers part of the {@link TaskAction} until another task completes.
	 * <p>
	 * The {@link TaskAction} implementation that calls this method should return from the action immediately afterwards.
	 * The {@link TaskAction} that is passed as a parameter will start running after the timeout has elapsed. It will
	 * run with the {@link TaskContext} that this method is being called on.
	 * </p><p>
	 * If the associated {@link TaskAction} is being executed synchronously, this method may block
	 * the calling thread for the timeout duration and then continue with the {@code continueAction}.
	 * </p>
	 * @param taskToWaitFor The task to wait for
	 * @param continueAction The {@link TaskAction} that will contiue running after the timeout
	 * @param canCancel If {@code true}, an attempt to cancel the associated task while waiting will start the action before the other task completes
	 * @throws TaskDeferredExecption When the task associated with this context is already deferred by a previous call to this method
	 * @throws NullPointerException When {@code unit} or {@code continueAction} is {@code null}
	 */
	public void setTimeout(Task taskToWaitFor, TaskAction continueAction, boolean canCancel);
	
	/**
	 * Defers part of the {@link TaskAction} until the {@link TaskCompleter} sends a success signal.
	 * If the completer sends a failure signal, the {@code continueAction} will never run.
	 * <p>
	 * The {@link TaskAction} implementation that calls this method should return from the action immediately afterwards.
	 * The {@link TaskAction} that is passed as a parameter will start running after the timeout has elapsed. It will
	 * run with the {@link TaskContext} that this method is being called on.
	 * </p><p>
	 * If the associated {@link TaskAction} is being executed synchronously, this method may block
	 * the calling thread for the timeout duration and then continue with the {@code continueAction}.
	 * </p>
	 * @param taskToWaitFor The {@link TaskCompleter} that will send the success signal
	 * @param continueAction The {@link TaskAction} that will contiue running after the timeout
	 * @param canCancel If {@code true}, an attempt to cancel the associated task while waiting will start the action before the completer is signalled
	 * @throws TaskDeferredExecption When the task associated with this context is already deferred by a previous call to this method
	 * @throws NullPointerException When {@code unit} or {@code continueAction} is {@code null}
	 */
	public void setTimeout(TaskCompleter taskToWaitFor, TaskAction continueAction, boolean canCancel);
	
	/**
	 * Fails the current {@link Task} by throwing a {@link TaskFailureRequestException} with the
	 * error message.
	 * @param message The message for the exception
	 * @throws TaskDeferredExecption When calling this method after any {@code setTimeout(..)} method
	 * @throws TaskFailureRequestException Always
	 */
	public void fail(String message) throws TaskFailureRequestException, TaskDeferredExecption;
}
