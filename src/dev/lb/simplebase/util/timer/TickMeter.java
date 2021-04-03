package dev.lb.simplebase.util.timer;

import java.util.concurrent.TimeUnit;

/**
 * Can be used to measure tick duration and tick rate
 */
public class TickMeter {

	private final TimeSource source;
	private final double averagingInterval;
	
	private double lastTickTimestamp;
	private double lastTickTime;
	
	private double lastAverageIntervalTimestamp;
	private double lastAverageTickTime;
	private int averageIntervalTickCount;
	
	/**
	 * Creates a new {@link TickMeter} with a time source.
	 * @param source The time source to use for measuring tick time
	 * @param averagingInterval The time interval in which averages are taken and updated
	 * @param averagingIntervalUnit The {@link TimeUnit} for that averaging interval
	 */
	public TickMeter(TimeSource source, double averagingInterval, TimeUnit averagingIntervalUnit) {
		this.source = source;
		this.averagingInterval = averagingInterval * TimeSource.factor(averagingIntervalUnit, source.getTimeUnit());
		lastTickTimestamp = -1;
		lastAverageIntervalTimestamp = -1;
		lastTickTime = 0;
		lastAverageTickTime = 0;
		lastAverageIntervalTimestamp = 0;
	}
	
	/**
	 * Average time (in seconds) that a tick took withing the averaging interval.
	 * <br> Is 0 before the averaging interval has passed at least once.
	 * @return The average time for one tick
	 */
	public double getAverageTickTime() {
		return getAverageTickTime(TimeUnit.SECONDS);
	}
	
	/**
	 * Average time in the requested time unit that a tick took withing the averaging interval.
	 * <br> Is 0 before the averaging interval has passed at least once.
	 * @param unit The requested {@link TimeUnit}
	 * @return The average time for one tick
	 */
	public double getAverageTickTime(TimeUnit unit) {
		return lastAverageTickTime * TimeSource.factor(source.getTimeUnit(), unit);
	}
	
	/**
	 * Time in seconds that the last recorded tick took
	 * <br> Is 0 when two ticks have not happened yet.
	 * @return The last time for one tick
	 */
	public double getLastTickTime() {
		return getAverageTickTime(TimeUnit.SECONDS);
	}
	
	/**
	 * Time in the requested time unit that the last recorded tick took.
	 * <br> Is 0 when two ticks have not happened yet.
	 * @param unit The requested {@link TimeUnit}
	 * @return The last time for one tick
	 */
	public double getLastTickTime(TimeUnit unit) {
		return lastTickTime * TimeSource.factor(source.getTimeUnit(), unit);
	}
	
	/**
	 * Average amount of ticks per second within the averaging interval.
	 * <br> Is {@code infinity} before the averaging interval has passed at least once.
	 * @return The average tick rate
	 */
	public double getAverageTickRate() {
		return getAverageTickRate(TimeUnit.SECONDS);
	}
	
	/**
	 * Average amount of ticks per time unit within the averaging interval.
	 * <br> Is {@code infinity} before the averaging interval has passed at least once.
	 * @param unit The requested {@link TimeUnit}
	 * @return The average tick rate
	 */
	public double getAverageTickRate(TimeUnit unit) {
		return 1.0d / getAverageTickTime(unit);
	}
	
	/**
	 * The interval of time in which ticks are considered when calculating the average.
	 * @param unit The time unit in which the interval should be expressed
	 * @return The relevant duration for avreage calculation
	 */
	public double getAveragingInterval(TimeUnit unit) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Count one tick. Delta time is automatically calculated using the meters time source
	 */
	public void tick() {
		final var timestamp = source.currentTime(); //Timestamp for this tick. Don not query time again!!!
		
		if(lastTickTimestamp == -1) { //first tick!
			lastTickTimestamp = timestamp;
			lastAverageIntervalTimestamp = timestamp;
		} else {
			lastTickTime = timestamp - lastTickTimestamp;
			lastTickTimestamp = timestamp;
			
			averageIntervalTickCount++;
			final var adjustedInterval = timestamp - lastAverageIntervalTimestamp;
			if(adjustedInterval > averagingInterval) { //update average values
				lastAverageTickTime = adjustedInterval / (double) averageIntervalTickCount; 
				
				averageIntervalTickCount = 0;
				lastAverageIntervalTimestamp = timestamp;
			}
		}
	}
}
