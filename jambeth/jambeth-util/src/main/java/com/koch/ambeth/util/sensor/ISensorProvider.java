package com.koch.ambeth.util.sensor;

public interface ISensorProvider
{
	ISensor lookup(String sensorName);
}
