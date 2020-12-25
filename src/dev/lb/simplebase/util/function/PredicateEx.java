package dev.lb.simplebase.util.function;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that can throw a checked exception
 * @param <T> The parameter type of the predicate used to 
 * @param <E> The type of (checked) exception that the method can throw
 */
@FunctionalInterface
public interface PredicateEx<T, E extends Throwable> {

	/**
	 * Functional method of {@link PredicateEx}.
	 * @param t The value that the predicate should test
	 * @return The result of the test
	 * @throws E When the test cannot complete normally
	 */
	public boolean test(T t) throws E;
	
	/**
	 * Creates a java {@link Predicate} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A java {@link Predicate} for the same function
	 * @throws NullPointerException When {@code wrapper} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public default Predicate<T> asPredicate(Function<E, ? extends RuntimeException> wrapper) {
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
	 * Creates a java {@link Predicate} for the same function that this predicate refers to. 
	 * Checked exceptions will be wrapped and rethrown
	 * @param <T> The result type of the predicate
	 * @param <E> The type of checked exception thrown by the {@link PredicateEx}
	 * @param predicateEx The {@link PredicateEx} that should be wrapped
	 * @param wrapper Creates a subtype of {@link RuntimeException} that is thrown for a a checked exception
	 * @return A java {@link Predicate} for the same function
	 * @throws NullPointerException When {@code predicatedEx} or {@code wrapper} is {@code null}
	 */
	public static <T, E extends Throwable> Predicate<T> asPredicate(PredicateEx<T, E> predicateEx, Function<E, ? extends RuntimeException> wrapper) {
		return Objects.requireNonNull(predicateEx, "'predicateEx' parameter must not be null").asPredicate(wrapper);
	}
	
}
