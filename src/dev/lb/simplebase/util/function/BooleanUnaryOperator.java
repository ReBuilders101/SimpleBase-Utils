package dev.lb.simplebase.util.function;

/**
 * A functional interface that accepts a single boolean parameter and produces a boolean result.
 */
@FunctionalInterface
public interface BooleanUnaryOperator {
	/**
	 * Functional method of {@link BooleanUnaryOperator}. 
	 * @param value The boolean parameter
	 * @return The produced result
	 */
	public boolean apply(boolean value);
	
}
