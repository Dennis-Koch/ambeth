package de.osthus.ambeth.sensor;

public class IntervalInfo
{
	protected final long startedTime;

	public IntervalInfo(long startedTime)
	{
		this.startedTime = startedTime;
	}

	public long getStartedTime()
	{
		return startedTime;
	}
}
