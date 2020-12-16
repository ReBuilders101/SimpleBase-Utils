package dev.lb.simplebase.util.value;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.lb.simplebase.util.function.BooleanConsumer;

/**
 * A container object which may or may not contain a {@code boolean} value.
 */
public final class OptionalBoolean {

	private static final OptionalBoolean EMPTY = new OptionalBoolean();


	private final boolean isPresent;
	private final boolean value;

	private OptionalBoolean() {
		this.isPresent = false;
		this.value = false;
	}

	/**
	 * Returns an empty {@code OptionalBoolean} instance.
	 * @return an empty {@code OptionalBoolean}
	 */
	public static OptionalBoolean empty() {
		return EMPTY;
	}

	private OptionalBoolean(boolean value) {
		this.isPresent = true;
		this.value = value;
	}

	/**
	 * Returns an {@code OptionalBoolean} with the given value.
	 * @param value The value in the {@link OptionalBoolean}
	 * @return an {@code OptionalBoolean} with the value present
	 */
	public static OptionalBoolean of(boolean value) {
		return new OptionalBoolean(value);
	}

	/**
	 * If a value is present, returns the value, otherwise throws
	 * {@code NoSuchElementException}.
	 * @return the value described by this {@code OptionalBoolean}
	 * @throws NoSuchElementException if no value is present
	 */
	public boolean getAsBoolean() {
		if (!isPresent) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * If a value is present, returns {@code true}, otherwise {@code false}.
	 * @return {@code true} if a value is present, otherwise {@code false}
	 */
	public boolean isPresent() {
		return isPresent;
	}

	/**
	 * If a value is not present, returns {@code true}, otherwise {@code false}.
	 * @return  {@code true} if a value is not present, otherwise {@code false}
	 */
	public boolean isEmpty() {
		return !isPresent;
	}

	/**
	 * If a value is present, performs the given action with the value,
	 * otherwise does nothing.
	 * @param action the action to be performed, if a value is present
	 * @throws NullPointerException if value is present and the given action is {@code null}
	 */
	public void ifPresent(BooleanConsumer action) {
		if (isPresent) {
			action.accept(value);
		}
	}

	/**
	 * If a value is present, runs the given consumer with the value,
	 * otherwise performs the given runnable.
	 * @param action the action to be performed if a value is present
	 * @param emptyAction the action to be performed if no value is present
	 * @throws NullPointerException if a value is present and the given action
	 *         is {@code null}, or no value is present and the given empty-based
	 *         action is {@code null}.
	 */
	public void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction) {
		if (isPresent) {
			action.accept(value);
		} else {
			emptyAction.run();
		}
	}

	/**
	 * An {@link Optional} with the boxed value of this {@link OptionalBoolean}.
	 * @return An {@link Optional} of {@link Boolean} that contains the same value as this {@link OptionalBoolean}
	 */
	public Optional<Boolean> boxed() {
		return isPresent ? Optional.of(value) : Optional.empty();
	}

	/**
	 * If a value is present, returns the value, otherwise returns {@code other}.
	 * @param other the value to be returned, if no value is present
	 * @return the value, if present, otherwise {@code other}
	 */
	public boolean orElse(boolean other) {
		return isPresent ? value : other;
	}

	/**
	 * If a value is present, returns the value, otherwise returns the result produced by the supplier.
	 * @param supplier the supplying function that produces a value to be returned
	 * @return the value, if present, otherwise the result produced by the supplier
	 * @throws NullPointerException if no value is present and the supplier is {@code null}
	 */
	public boolean orElseGet(BooleanSupplier supplier) {
		return isPresent ? value : supplier.getAsBoolean();
	}

	/**
	 * If a value is present, returns the value, otherwise throws {@code NoSuchElementException}.
	 * @return the value described by this {@code OptionalBoolean}
	 * @throws NoSuchElementException if no value is present
	 */
	public boolean orElseThrow() {
		if (!isPresent) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * If a value is present, returns the value, otherwise throws an exception produced by the exception supplier.
	 * @param <X> Type of the exception to be thrown
	 * @param exceptionSupplier the supplying function that produces an exception to be thrown
	 * @return the value in this supplier, if present
	 * @throws X when no value is present
	 * @throws NullPointerException if no value is present and the exception supplier is {@code null}
	 */
	public<X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (isPresent) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof OptionalBoolean)) {
			return false;
		}
		
		final OptionalBoolean other = (OptionalBoolean) obj;
		return (isPresent && other.isPresent)
				? value == other.value
				: isPresent == other.isPresent;
	}

	/**
	 * Returns the hash code of the value if present (using {@link Boolean#hashCode(boolean)}),
	 * otherwise 0 if no value is present.
	 * @return hash code value of the present value or {@code 0} if no value is present
	 */
	@Override
	public int hashCode() {
		return isPresent ? Boolean.hashCode(value) : 0;
	}

	@Override
	public String toString() {
		return isPresent ? String.format("OptionalBoolean[%s]", value) : "OptionalBoolean.empty";
	}

}
