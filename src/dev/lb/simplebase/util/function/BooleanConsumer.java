package dev.lb.simplebase.util.function;

import java.util.function.Consumer;

/**
 * A functional interface that accepts a single boolean paranmeter and returns no result.
 */
@FunctionalInterface
public interface BooleanConsumer {

	/**
	 * Functional method of {@link BooleanConsumer}. 
	 * @param value The boolean parameter
	 */
	public void accept(boolean value);
	
	/**
	 * A boxed version of this primitive consumer
	 * @return A {@link Consumer} of {@link Boolean} that represents this consumer
	 */
	public default Consumer<Boolean> boxed() {
		return this::accept;
	}
}
