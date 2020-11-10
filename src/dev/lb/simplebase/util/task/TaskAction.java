package dev.lb.simplebase.util.task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * A functional interface for an action that can be represented by a {@link Task}.
 */
@FunctionalInterface
public interface TaskAction {

	/**
	 * The functional method of {@link TaskAction}.
	 * <p>
	 * The action receives a {@link TaskContext} to handle cancellation requests and
	 * may throw any type of checked or unchecked {@link Exception}.
	 * </p> 
	 * @param context The {@link TaskContext} for the task
	 * @throws Exception Any exception that will cause the task to fail
	 */
	public void run(TaskContext context) throws Exception;
	
	/**
	 * Makes a {@link Runnable} executable as a {@link Task}.
	 * @param runnable The runnable to run as a task
	 * @return A {@link TaskAction} for that runnable
	 */
	public static TaskAction of(Runnable runnable) {
		return ctx -> runnable.run();
	}
	
	/**
	 * Makes a {@link Consumer} executable as a {@link Task}.
	 * @param consumer The runnable to run as a task
	 * @return A {@link TaskAction} for that consumer
	 */
	public static TaskAction of(Consumer<TaskContext> consumer) {
		return ctx -> consumer.accept(ctx);
	}
	
	/**
	 * Makes a {@link Callable} executable as a {@link Task}.
	 * The return value of the callable will be ignored.
	 * @param callable The runnable to run as a task
	 * @return A {@link TaskAction} for that callable
	 */
	public static TaskAction of(Callable<?> callable) {
		return ctx -> callable.call();
	}
}
