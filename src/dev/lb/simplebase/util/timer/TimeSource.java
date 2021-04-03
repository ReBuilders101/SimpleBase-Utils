package dev.lb.simplebase.util.timer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A {@link TimeSource} provides an internal time measure (such as the system clock) and
 * provides this time in a fixed unit
 */
public interface TimeSource {

	/**
	 * The value of the timer at the time of method invocation.<br>
	 * Successive invocations should produce equal or increasing results.
	 * @return The current timer value
	 */
	public double currentTime();
	/**
	 * The {@link TimeUnit} of the provided time value.
	 * <br><b>Must be constant for one instance/implementation!</b>
	 * @return The unit of time for this timer
	 */
	public TimeUnit getTimeUnit();

	/**
	 * Creates a new timer that uses the same source, but provides that
	 * time in a different time unit.
	 * @param unit The {@link TimeUnit} for the new timer
	 * @return A new {@link TimeSource}
	 */
	public default TimeSource as(TimeUnit unit) {
		if(Objects.requireNonNull(unit, "'unit' parameter must not be null") == getTimeUnit()) {
			return this;
		} else {
			return new MappedTimeSource(this, unit);
		}
	}
	
	/**
	 * A default {@link TimeSource} using {@link System#currentTimeMillis()}
	 */
	public static final TimeSource SYSTEM_TIME = new TimeSource() {
		
		@Override
		public TimeUnit getTimeUnit() {
			return TimeUnit.MILLISECONDS;
		}

		@Override
		public double currentTime() {
			return System.currentTimeMillis();
		}
	};

	/**
	 * A default {@link TimeSource} using {@link System#nanoTime()}
	 */
	public static final TimeSource NANO_TIME = new TimeSource() {

		@Override
		public TimeUnit getTimeUnit() {
			return TimeUnit.NANOSECONDS;
		}

		@Override
		public double currentTime() {
			return System.nanoTime();
		}
	};
	
	/**
	 * Calculate conversion factor between two time units
	 * @param from {@link TimeUnit} to convert from
	 * @param to {@link TimeUnit} to convert to
	 * @return The factor to convert time values
	 */
	public static double factor(TimeUnit from, TimeUnit to) {
		if(Objects.requireNonNull(from) == Objects.requireNonNull(to)) return 1;
		final var toThis = to.convert(1, from);
		if(toThis != 0L) {
			return (double) toThis;
		} else {
			return 1.0d / (double) from.convert(1, to);
		}
	}
}
