package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * A task that is done at creation time
 */
@Internal
abstract class DoneTask implements Task{

	@Override
	public final boolean cancel(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean cancelIfRunning(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean cancelIfNotStarted(/*Nullable*/ Object exceptionPayload) {
		return false;
	}

	@Override
	public final boolean isPrevented() {
		return false;
	}
	
	@Override
	public final boolean isDone() {
		return true;
	}

	@Override
	public final boolean isRunning() {
		return false;
	}
	
	@Override
	public final boolean isStartable() {
		return false;
	}

	@Override
	public final boolean isSynchronous() {
		return true;
	}
	
	@Override
	public final Task await() throws InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await()");
		return this;
	}

	@Override
	public final Task awaitUninterruptibly() {
		return this;
	}

	@Override
	public final Task await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit)");
		return this;
	}

	@Override
	public final Task awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		return this;
	}

	@Override
	public final Task await(CancelCondition condition) throws InterruptedException, CancelledException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(CancelCondition)");
		return this;
	}

	@Override
	public final Task awaitUninterruptibly(CancelCondition condition) throws CancelledException {
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		return this;
	}

	@Override
	public final Task await(long timeout, TimeUnit unit, CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		if(Thread.interrupted()) throw new InterruptedException("Thread had interruped status set when entering Task.await(long, TimeUnit, CancelCondition)");
		return this;
	}

	@Override
	public final Task awaitUninterruptibly(long timeout, TimeUnit unit, CancelCondition condition) throws TimeoutException, CancelledException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		return this;
	}

	@Override
	public final boolean isCancellationExpired() {
		return true;
	}
	
	@Override
	public final Task onCompletion(Consumer<Task> action) {
		Objects.requireNonNull(action, "'action' for onCompletion must not be null");
		action.accept(this);
		return this;
	}

	@Override
	public final Task onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		Objects.requireNonNull(action, "'action' for onCompletionAsync must not be null");
		Objects.requireNonNull(executor, "'executor' for onCompletionAsync must not be null");
		executor.submit(() -> action.accept(this));
		return this;
	}
	
	@Override
	public final boolean startAsync() throws CancelledException, RejectedExecutionException {
		return false;
	}
	
	@Override
	public final boolean startAsync(ExecutorService executor) throws CancelledException, RejectedExecutionException {
		Objects.requireNonNull(executor, "'executor' for startAsync must not be null");
		return false;
	}

	@Override
	public final boolean startSync() throws CancelledException {
		return false;
	}
	
	@Override
	public final boolean executeSync() throws CancelledException, Throwable {
		return false;
	}
}
