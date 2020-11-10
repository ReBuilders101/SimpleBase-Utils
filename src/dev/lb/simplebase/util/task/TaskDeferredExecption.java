package dev.lb.simplebase.util.task;

/**
 * Thrown when {@link TaskContext#setTimeout(long, java.util.concurrent.TimeUnit, TaskAction)} is
 * called on a task that has already been deferred with that method and has not yet resumed.
 */
public class TaskDeferredExecption extends RuntimeException{
	private static final long serialVersionUID = -7910562415985449383L;

	TaskDeferredExecption() {
		super("Cannot call setTimeout more than once in the same TaskAction");
	}
	
}
