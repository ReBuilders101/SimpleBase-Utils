package dev.lb.simplebase.util.value;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.lb.simplebase.util.annotation.Threadsafe;
import dev.lb.simplebase.util.annotation.ValueType;

/**
 * Container type for a threadsafe reference to a non-null value that can be assigned once and then never changed again.
 * @param <T> The type of the contained value
 */
@Threadsafe
@ValueType
public class AssignOnce<T> {

	private volatile T ref;
	private final StampedLock lock;
	
	/**
	 * Creates a new instance of {@link AssignOnce} with no associated values.
	 */
	public AssignOnce() {
		this.ref = null;
		this.lock = new StampedLock();
	}
	
	/**
	 * Attempts to assign a value and returns a context object on success. Returns {@code null} instead if a value
	 * is already present and a new one cannot be assigned. The {@link Supplier} and {@link Function} will only be executed
	 * if the value can be assigned successfully.
	 * @param <Context> The type of context object
	 * @param ctx The supplier that creates the context object
	 * @param value The function that produces the value from the context object
	 * @return The context object, or {@code null} if the value could not be assigned
	 * @throws NullPointerException When {@code ctx} or {@code value} is {@code null}, or when {@code ctx} or {@code value} return {@code null} when called
	 */
	public <Context> Context tryAssignWithContext(Supplier<Context> ctx, Function<Context, T> value) throws NullPointerException {
		Objects.requireNonNull(ctx, "'ctx' parameter must not be null");
		Objects.requireNonNull(value, "'value' parameter must not be null");
		//Double-check
		if(isAssigned()) return null;
		final long stamp = lock.writeLock();
		try {
			if(isAssigned()) return null;
			var con = Objects.requireNonNull(ctx.get(), "'ctx' supplier must not return null"); //must assign non-null
			ref = Objects.requireNonNull(value.apply(con), "'value' function must not return null");
			return con;
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	/**
	 * Attempts to assign a value and returns {@code true} on success. Returns {@code false} instead if a value
	 * is already present and a new one cannot be assinged.
	 * @param value The non-null value to assign
	 * @return {@code true} on success, {@code false} if a value was already present
	 * @throws NullPointerException When the value that should be assigned is {@code null}
	 */
	public boolean tryAssign(final T value) throws NullPointerException {
		Objects.requireNonNull(value, "'value' parameter must not be null");
		//Double-check
		if(isAssigned()) return false;
		final long stamp = lock.writeLock();
		try {
			if(isAssigned()) return false;
			ref = value;
			return true;
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	/**
	 * Attempts to assign a value and returns {@code true} on success. Returns {@code false} instead if a value
	 * is already present and a new one cannot be assigned. The {@link Supplier} will only be executed
	 * if the value can be assigned successfully.
	 * @param value The {@link Supplier} that creates the value. MAy have side effects, as it will only run if the value is actually assigned
	 * @return {@code true} on success, {@code false} if a value was already present
	 * @throws NullPointerException When the value that should be assigned is {@code null}, or the supplier is {@code null}
	 */
	public boolean tryAssign(final Supplier<T> value) throws NullPointerException {
		Objects.requireNonNull(value, "'value' parameter must not be null");
		//Double-check
		if(isAssigned()) return false;
		final long stamp = lock.writeLock();
		try {
			if(isAssigned()) return false;
			ref = Objects.requireNonNull(value.get(), "'value' supplier must not return null"); //must assign non-null
			return true;
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	/**
	 * Whether a value has been assigned.
	 * <p>
	 * If this method returns {@code true}, this means that all subsequent calls to any of the {@code get...} methods
	 * will succeed (as the value is present and cannot get un-set).<br>
	 * However a return value of {@code false} does not guarantee that a subsequento {@link #tryAssign(Object)} will succeed,
	 * as the value could have been set by a concurrent thread between those two calls. 
	 * </p>
	 * @return {@code true} if a value is assigned, {@code false} otherwise
	 */
    public boolean isAssigned() {
		return ref != null;
	}
	
    /**
     * Returns an {@link Optional} with the contained value, or an empty {@link Optional} if no value is assigned.
     * @return An {@link Optional} with the contained value, or an empty {@link Optional} if no value is assigned
     */
	public Optional<T> getOptional() {
		return Optional.ofNullable(getNullable());
	}
	
	/**
	 * Returns the assigned value or throws an exception if no value is assigned.
	 * @return The assigned value
	 * @throws IllegalStateException When no value has been assigned
	 */
	public T getValue() throws IllegalStateException {
		var v = getNullable();
		if(v == null) throw new IllegalStateException("No value assigned");
		return v;
	}
	
	/**
	 * Returns The assigned value, or {@code null} if no value is assigned.
	 * @return The assigned value, or {@code null} if no value is assigned
	 */
	public T getNullable() {
		return ref;
	}

	@Override
	public String toString() {
		if(ref == null) {
			return "AssignOnce [unassigned]";
		} else {
			return "AssignOnce [value=" + ref + "]"; //ref is never null
		}
	}
	
}
