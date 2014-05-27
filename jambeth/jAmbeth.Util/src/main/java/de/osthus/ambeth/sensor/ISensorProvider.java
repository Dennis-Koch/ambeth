package de.osthus.ambeth.sensor;

public interface ISensorProvider
{
	ISensor lookup(String sensorName);
}
