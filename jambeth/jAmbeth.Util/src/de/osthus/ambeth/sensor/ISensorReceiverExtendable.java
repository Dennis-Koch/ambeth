package de.osthus.ambeth.sensor;

public interface ISensorReceiverExtendable
{
	void registerSensorReceiver(ISensorReceiver sensorReceiver, String sensorName);

	void unregisterSensorReceiver(ISensorReceiver sensorReceiver, String sensorName);
}
