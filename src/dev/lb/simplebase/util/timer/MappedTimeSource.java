package dev.lb.simplebase.util.timer;

import java.util.concurrent.TimeUnit;

/*package*/ class MappedTimeSource implements TimeSource {

	private final TimeSource source;
	private final double conversion;
	private final TimeUnit unit;
	
	MappedTimeSource(TimeSource source, TimeUnit newUnit) {
		this.source = source;
		this.unit = newUnit;
		this.conversion = TimeSource.factor(source.getTimeUnit(), newUnit);
	}
	
	@Override
	public double currentTime() {
		return source.currentTime() * conversion;
	}

	@Override
	public TimeUnit getTimeUnit() {
		return unit;
	}

	@Override
	public TimeSource as(TimeUnit timeUnit) {
		return source.as(timeUnit);
	}
	
}