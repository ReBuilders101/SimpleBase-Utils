package dev.lb.simplebase.util.function;

/**
 * A functional interface that accepts a single boolean parameter and produces an object result.
 * @param <T> The type of result for the functional method
 */
@FunctionalInterface
public interface BooleanFunction<T> {
	/**
	 * Functional method of {@link BooleanFunction}. 
	 * @param value The boolean parameter
	 * @return The produced object
	 */
	public T apply(boolean value);
}
