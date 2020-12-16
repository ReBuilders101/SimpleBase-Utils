package dev.lb.simplebase.util.function;

/**
 * A functional interface that accepts a single boolean parameter and produces an object result.
 * @param <T> The type of result for the functional method
 */
@FunctionalInterface
public interface BooleanToObjectFunction<T> {
	/**
	 * Functional method of {@link BooleanToObjectFunction}. 
	 * @param value The boolean parameter
	 * @return The produced object
	 */
	public T applyBoolean(boolean value);
}
