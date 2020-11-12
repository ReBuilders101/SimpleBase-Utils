package dev.lb.simplebase.util.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class ConcurrentActionTask extends BlockingTask {

	private final TaskAction action;
	private final TaskContext context;
	private volatile ExecutorService taskExecutor;
	
	private final AtomicInteger state;
	/*
	 * Flags:
	 * 1 -> Member access valid
	 * 2 -> Cancelled
	 * 4 -> Success
	 * 8 -> Failure
	 * 16 -> CanStart
	 */
	private static final int INITIALIZED = 17;
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
	
	
	ConcurrentActionTask(TaskAction action) {
		throw new UnsupportedOperationException("Cannot create TaskAction tasks");
	}
	
	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSuccessful() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isStartable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isPrevented() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSynchronous() {
		return taskExecutor == null;
	}
	
	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean cancel(Object exceptionPayload) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Task checkFailure() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <E extends Throwable> Task checkFailure(Class<E> expectedType) throws E {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Throwable getFailure() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasUnconsumedException() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Task checkSuccess() throws TaskFailureException, CancelledException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean startAsync() throws CancelledException, RejectedExecutionException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean startAsync(ExecutorService executor) throws CancelledException, RejectedExecutionException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean startSync() throws CancelledException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean executeSync() throws CancelledException, Throwable {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelIfRunning(Object exceptionPayload) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean cancelIfNotStarted(Object exceptionPayload) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isCancellationExpired() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public CancelledException getCancellationException() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	private class Context implements TaskContext {

		@Override
		public boolean shouldCancel() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void confirmCancelled() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isDeferred() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setTimeout(long timeout, TimeUnit unit, TaskAction continueAction, boolean canCancel)
				throws TaskDeferredExecption {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTimeout(Task taskToWaitFor, TaskAction continueAction, boolean canCancel) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTimeout(TaskCompleter taskToWaitFor, TaskAction continueAction, boolean canCancel) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fail(String message) throws TaskFailureRequestException, TaskDeferredExecption {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
