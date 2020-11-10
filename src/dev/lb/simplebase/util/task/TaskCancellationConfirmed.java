package dev.lb.simplebase.util.task;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * A {@link TaskCancellationConfirmed} is thrown by {@link TaskContext#confirmCancelled()}
 * and must not be caught by task code to confirm that the task was cancelled.
 * <p>
 * This is a subclass of {@link Error} to prevent it from being caught with {@link Exception}s or {@link RuntimeException}s.
 * </p>
 */
@Internal
class TaskCancellationConfirmed extends Error {
	private static final long serialVersionUID = -8714509437488553432L;
	
	TaskCancellationConfirmed() {
		super("Task cancelled");
	}
}
