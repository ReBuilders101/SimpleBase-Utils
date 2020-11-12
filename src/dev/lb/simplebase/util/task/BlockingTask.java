package dev.lb.simplebase.util.task;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

abstract class BlockingTask<T> implements TaskOf<T> {

	protected final Awaiter awaiter;
	protected final SubscriptionHandler<CancelledException> onCancelled;
	protected final SubscriptionHandler<T> onSuccess;
	protected final SubscriptionHandler<Throwable> onFailure;
	protected final SubscriptionHandler<Task> onCompletion;
	
	protected BlockingTask() {
		this.awaiter = new Awaiter();
		this.onCancelled = new SubscriptionHandler<>();
		this.onSuccess = new SubscriptionHandler<>();
		this.onFailure = new SubscriptionHandler<>();
		this.onCompletion = new SubscriptionHandler<>();
	}
	
	@Override
	public TaskOf<T> await() throws InterruptedException {
		awaiter.await(Awaiter.MASTER_PERMIT);
		return this;
	}

	@Override
	public TaskOf<T> awaitUninterruptibly() {
		awaiter.awaitUninterruptibly(Awaiter.MASTER_PERMIT);
		return this;
	}
	
	@Override
	public TaskOf<T> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		awaiter.await(Awaiter.MASTER_PERMIT, timeout, unit);
		return this;
	}

	@Override
	public TaskOf<T> awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		awaiter.awaitUninterruptibly(Awaiter.MASTER_PERMIT, timeout, unit);
		return this;
	}

	@Override
	public TaskOf<T> await(CancelCondition condition) throws InterruptedException, CancelledException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");

		//Use the condition as the signal object
		condition.onCancelled((ex) -> awaiter.signalAll(condition));

		if(awaiter.await(condition) == condition) { //If cancelled (and not completed normally)
			throw condition.getCancellationException(); //Throw the cancellation cause
		}

		return this;
	}

	@Override
	public TaskOf<T> awaitUninterruptibly(CancelCondition condition) throws CancelledException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");

		//Use the condition as the signal object
		condition.onCancelled((ex) -> awaiter.signalAll(condition));
		if(awaiter.awaitUninterruptibly(condition) == condition) { //If cancelled (and not completed normally)
			throw condition.getCancellationException(); //Throw the cancellation cause
		}
		return this;
	}

	@Override
	public TaskOf<T> await(long timeout, TimeUnit unit, CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		//Use the condition as the signal object
		condition.onCancelled((ex) -> awaiter.signalAll(condition));
		if(awaiter.await(condition, timeout, unit) == condition) { //If cancelled (and not completed normally)
			throw condition.getCancellationException(); //Throw the cancellation cause
		}
		return this;
	}

	@Override
	public TaskOf<T> awaitUninterruptibly(long timeout, TimeUnit unit, CancelCondition condition) throws TimeoutException, CancelledException {
		//cannot use null as that is the master permit
		Objects.requireNonNull(condition, "'condition' parameter must not be null");
		Objects.requireNonNull(unit, "'unit' parameter must not be null");

		//Use the condition as the signal object
		condition.onCancelled((ex) -> awaiter.signalAll(condition));
		if(awaiter.awaitUninterruptibly(condition, timeout, unit) == condition) { //If cancelled (and not completed normally)
			throw condition.getCancellationException(); //Throw the cancellation cause
		}
		return this;
	}
	

	@Override
	public TaskOf<T> onCancelled(Consumer<CancelledException> action) {
		onCancelled.subscribe(action);
		return this;
	}

	@Override
	public TaskOf<T> onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
		onCancelled.subscribeAsync(action, executor);
		return this;
	}

	@Override
	public TaskOf<T> onSuccess(Runnable action) {
		onSuccess.subscribeRunnable(action);
		return this;
	}

	@Override
	public TaskOf<T> onSuccessAsync(Runnable action, ExecutorService executor) {
		onSuccess.subscribeRunnableAsync(action, executor);
		return this;
	}

	@Override
	public TaskOf<T> onFailure(Consumer<Throwable> action) {
		onFailure.subscribe(action);
		return this;
	}

	@Override
	public TaskOf<T> onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
		onFailure.subscribeAsync(action, executor);
		return this;
	}

	@Override
	public TaskOf<T> onCompletion(Consumer<Task> action) {
		onCompletion.subscribe(action);
		return this;
	}

	@Override
	public TaskOf<T> onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		onCompletion.subscribeAsync(action, executor);
		return this;
	}
	

	@Override
	public TaskOf<T> onSuccess(Consumer<T> action) {
		onSuccess.subscribe(action);
		return this;
	}

	@Override
	public TaskOf<T> onSuccessAsync(Consumer<T> action, ExecutorService executor) {
		onSuccess.subscribeAsync(action, executor);
		return this;
	}
}
