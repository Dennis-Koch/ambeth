package de.osthus.ambeth.sensor;

public interface ISensorReceiver
{
	void touch(String sensorName, Object... additionalData);

	void touch(String sensorName);

	void on(String sensorName, Object... additionalData);

	void on(String sensorName);

	void off(String sensorName);
}
