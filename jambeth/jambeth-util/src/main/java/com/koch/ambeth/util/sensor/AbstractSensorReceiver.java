package com.koch.ambeth.util.sensor;

public abstract class AbstractSensorReceiver implements ISensorReceiver
{
	@Override
	public void off(String sensorName)
	{
		// Intended blank
	}

	@Override
	public void on(String sensorName)
	{
		// Intended blank
	}

	@Override
	public void on(String sensorName, Object... additionalData)
	{
		// Intended blank
	}

	@Override
	public void touch(String sensorName)
	{
		// Intended blank
	}

	@Override
	public void touch(String sensorName, Object... additionalData)
	{
		// Intended blank
	}
}
