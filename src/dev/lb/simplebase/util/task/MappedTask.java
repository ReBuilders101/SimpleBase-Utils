package dev.lb.simplebase.util.task;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import dev.lb.simplebase.util.annotation.Internal;

@Internal
final class MappedTask<T, R> implements TaskOf<R> {

	private final TaskOf<T> task;
	private final Function<T, R> func;
	
	MappedTask(TaskOf<T> delagateTask, Function<T, R> mapFunction) {
		this.task = delagateTask;
		this.func = mapFunction;
	}
	
	@Override
	public boolean isDone() {
		return task.isDone();
	}
	@Override
	public boolean isRunning() {
		return task.isRunning();
	}
	@Override
	public boolean isCancelled() {
		return task.isCancelled();
	}
	@Override
	public boolean isSuccessful() {
		return task.isSuccessful();
	}
	@Override
	public boolean isFailed() {
		return task.isFailed();
	}
	@Override
	public boolean isPrevented() {
		return task.isPrevented();
	}
	@Override
	public boolean isSynchronous() {
		return task.isSynchronous();
	}
	@Override
	public State getState() {
		return task.getState();
	}
	@Override
	public boolean cancel(Object exceptionPayload) {
		return task.cancel(exceptionPayload);
	}
	@Override
	public Throwable getFailure() {
		return task.getFailure();
	}
	@Override
	public boolean hasUnconsumedException() {
		return task.hasUnconsumedException();
	}
	@Override
	public <E extends Throwable> E getFailure(Class<E> expectedType) throws ClassCastException {
		return task.getFailure(expectedType);
	}
	@Override
	public boolean cancelIfRunning(Object exceptionPayload) {
		return task.cancelIfRunning(exceptionPayload);
	}
	@Override
	public boolean cancelIfNotStarted(Object exceptionPayload) {
		return task.cancelIfNotStarted(exceptionPayload);
	}
	@Override
	public boolean isCancellationExpired() {
		return task.isCancellationExpired();
	}
	@Override
	public CancelledException getCancellationException() {
		return task.getCancellationException();
	}
	@Override
	public TaskOf<R> onSuccess(Consumer<R> action) {
		task.onSuccess((t) -> action.accept(func.apply(t)));
		return this;
	}
	@Override
	public TaskOf<R> onSuccessAsync(Consumer<R> action, ExecutorService executor) {
		task.onSuccessAsync((t) -> action.accept(func.apply(t)), executor);
		return this;
	}
	@Override
	public TaskOf<R> await() throws InterruptedException {
		task.await();
		return this;
	}
	@Override
	public TaskOf<R> awaitUninterruptibly() {
		task.awaitUninterruptibly();
		return this;
	}
	@Override
	public TaskOf<R> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		task.await(timeout, unit);
		return this;
	}
	@Override
	public TaskOf<R> awaitUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
		task.awaitUninterruptibly(timeout, unit);
		return this;
	}
	@Override
	public TaskOf<R> await(CancelCondition condition) throws InterruptedException, CancelledException {
		task.await(condition);
		return this;
	}
	@Override
	public TaskOf<R> awaitUninterruptibly(CancelCondition condition) throws CancelledException {
		task.awaitUninterruptibly(condition);
		return this;
	}
	@Override
	public TaskOf<R> await(long timeout, TimeUnit unit, CancelCondition condition) throws InterruptedException, TimeoutException, CancelledException {
		task.await(timeout, unit, condition);
		return this;
	}
	@Override
	public TaskOf<R> awaitUninterruptibly(long timeout, TimeUnit unit, CancelCondition condition) throws TimeoutException, CancelledException {
		task.awaitUninterruptibly(timeout, unit, condition);
		return this;
	}
	@Override
	public TaskOf<R> checkFailure() throws Throwable {
		task.checkFailure();
		return this;
	}
	@Override
	public <E extends Throwable> TaskOf<R> checkFailure(Class<E> expectedType) throws E {
		task.checkFailure(expectedType);
		return this;
	}
	@Override
	public TaskOf<R> checkSuccess() throws TaskFailureException, CancelledException {
		task.checkSuccess();
		return this;
	}
	@Override
	public TaskOf<R> onCancelled(Consumer<CancelledException> action) {
		task.onCancelled(action);
		return this;
	}
	@Override
	public TaskOf<R> onCancelledAsync(Consumer<CancelledException> action, ExecutorService executor) {
		task.onCancelledAsync(action, executor);
		return this;
	}
	@Override
	public TaskOf<R> onSuccess(Runnable action) {
		task.onSuccess(action);
		return this;
	}
	@Override
	public TaskOf<R> onSuccessAsync(Runnable action, ExecutorService executor) {
		task.onSuccessAsync(action, executor);
		return this;
	}
	@Override
	public TaskOf<R> onFailure(Consumer<Throwable> action) {
		task.onFailure(action);
		return this;
	}
	@Override
	public TaskOf<R> onFailureAsync(Consumer<Throwable> action, ExecutorService executor) {
		task.onFailureAsync(action, executor);
		return this;
	}
	@Override
	public TaskOf<R> onCompletion(Consumer<Task> action) {
		task.onCompletion(action);
		return this;
	}
	@Override
	public TaskOf<R> onCompletionAsync(Consumer<Task> action, ExecutorService executor) {
		task.onCompletionAsync(action, executor);
		return this;
	}
	@Override
	public Optional<R> getFinishedResult() {
		return task.getFinishedResult().map(func);
	}
	@Override
	public R getResult() {
		return func.apply(task.getResult());
	}
	
}
