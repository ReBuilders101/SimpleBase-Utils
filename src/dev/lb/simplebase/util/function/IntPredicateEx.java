package dev.lb.simplebase.util.function;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * An {@link IntPredicate} that can throw a checked exception.
 * @param <E> The exception type
 */
@FunctionalInterface
public interface IntPredicateEx<E extends Throwable> {

	/**
	 * Functional method of {@link IntPredicateEx}.
	 * @param value The value to test
	 * @return The test result
	 * @throws E When the tast cannot complete normally
	 */
	public boolean test(int value) throws E;
	
	/**
	 * Creates a java {@link IntPredicate} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A java {@link IntPredicate} for the same function
	 * @throws NullPointerException When {@code wrapper} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public default IntPredicate asPredicate(Function<E, ? extends RuntimeException> wrapper) {
		Objects.requireNonNull(wrapper, "'wrapper' parameter must not be null");
		return t -> {
			try {
				return test(t);
			} catch (Throwable e) {
				if(e instanceof RuntimeException) throw (RuntimeException) e;
				if(e instanceof Error) throw (Error) e;
				throw wrapper.apply((E) e); //E is the only checked ex type that test() can produce
			}
		};
	}
	
	/**
	 * Creates a java {@link IntPredicate} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param <E> The type of checked exception thrown by the {@link IntPredicateEx}
	 * @param predicateEx The {@link IntPredicateEx} that should be wrapped
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A java {@link IntPredicate} for the same function
	 * @throws NullPointerException When {@code predicatedEx} or {@code wrapper} is {@code null}
	 */
	public static <E extends Throwable> IntPredicate asPredicate(IntPredicateEx<E> predicateEx, Function<E, ? extends RuntimeException> wrapper) {
		return Objects.requireNonNull(predicateEx, "'predicateEx' parameter must not be null").asPredicate(wrapper);
	}
}
