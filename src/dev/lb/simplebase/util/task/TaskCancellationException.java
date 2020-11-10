package dev.lb.simplebase.util.task;

import java.util.Optional;

import dev.lb.simplebase.util.annotation.Internal;

/**
 * A {@link TaskCancellationException} is thrown when a {@link Task} or a wating operation on
 * a task (such as {@link Task#await(CancelCondition)} is cancelled with a {@link CancelCondition}.
 */
public final class TaskCancellationException extends Exception {
	private static final long serialVersionUID = -8238488721299782503L;
	
	private final Object payload;
	
	/**
	 * Creates a new {@link TaskCancellationException}.
	 * @param message A non-{@code null} error message
	 * @param payload A payload object to store with the exception. May be {@code null}
	 */
	@Internal
	TaskCancellationException(Object payload) {
		super("Task was cancelled before completion");
		this.payload = payload;
	}

	/**
	 * The payload object attached to this exception. May be {@code null}.
	 * @return The payload object attached to this exception
	 */
	public Object getPayload() {
		return payload;
	}
	
	/**
	 * The payload object attached to this exception. May be {@code null}.
	 * <p>
	 * Casts the payload to the type requested in the parameter.
	 * </p>
	 * @param payloadType The class of the payload type
	 * @param <T> The type of the payload
	 * @return The payload object attached to this exception
	 * @throws ClassCastException When the requested and actual payload type are incompatible
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPayload(Class<T> payloadType) {
		return (T) payload;
	}
	
	/**
	 * The payload object attached to this exception. May be {@code null}.
	 * If the payload was initialized as {@code null}, the {@link Optional} will be empty.
	 * @return The payload object attached to this exception
	 */
	public Optional<Object> getOptionalPayload() {
		return Optional.ofNullable(payload);
	}
	
	/**
	 * The payload object attached to this exception.
	 * If the payload was initialized as {@code null}, the {@link Optional} will be empty.
	 * <p>
	 * Casts the payload to the type requested in the parameter.
	 * </p>
	 * @param payloadType The class of the payload type
	 * @param <T> The type of the payload
	 * @return The payload object attached to this exception
	 * @throws ClassCastException When the requested and actual payload type are incompatible
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getOptionalPayload(Class<T> payloadType) {
		return Optional.ofNullable((T) payload);
	}
	
}
