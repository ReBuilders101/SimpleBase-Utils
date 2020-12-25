package dev.lb.simplebase.util.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link BooleanUnaryOperator} that can throw a checked exception
 * @param <E> The exception type
 */
@FunctionalInterface
public interface BooleanUnaryOperatorEx<E extends Throwable> {
	
	/**
	 * Functional method of {@link BooleanUnaryOperatorEx}. 
	 * @param value The boolean parameter
	 * @return The produced result
	 * @throws E When the result cannot be produced normally
	 */
	public boolean apply(boolean value) throws E;
	
	/**
	 * Creates a regular {@link BooleanUnaryOperator} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A regular {@link BooleanUnaryOperator} for the same function
	 * @throws NullPointerException When {@code wrapper} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public default BooleanUnaryOperator asOperator(Function<E, ? extends RuntimeException> wrapper) {
		Objects.requireNonNull(wrapper, "'wrapper' parameter must not be null");
		return t -> {
			try {
				return apply(t);
			} catch (Throwable e) {
				if(e instanceof RuntimeException) throw (RuntimeException) e;
				if(e instanceof Error) throw (Error) e;
				throw wrapper.apply((E) e); //E is the only checked ex type that test() can produce
			}
		};
	}
	
	/**
	 * Creates a regular {@link BooleanUnaryOperator} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param <E> The type of checked exception thrown by the {@link BooleanUnaryOperatorEx}
	 * @param buoEx The {@link BooleanUnaryOperatorEx} that should be wrapped
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A regular {@link BooleanUnaryOperator} for the same function
	 * @throws NullPointerException When {@code predicatedEx} or {@code wrapper} is {@code null}
	 */
	public static <E extends Throwable> BooleanUnaryOperator asOperator(BooleanUnaryOperatorEx<E> buoEx, Function<E, ? extends RuntimeException> wrapper) {
		return Objects.requireNonNull(buoEx, "'predicateEx' parameter must not be null").asOperator(wrapper);
	}
}
