package com.koch.ambeth.util.sensor;

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
