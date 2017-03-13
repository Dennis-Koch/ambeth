package com.koch.ambeth.sensor;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.sensor.ISensor;
import com.koch.ambeth.util.sensor.ISensorProvider;
import com.koch.ambeth.util.sensor.ISensorReceiver;
import com.koch.ambeth.util.sensor.ISensorReceiverExtendable;

public class SensorProvider implements ISensorProvider, IInitializingBean, ISensorReceiverExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final LinkedHashMap<String, List<ISensorReceiver>> nameToSensorsMap = new LinkedHashMap<String, List<ISensorReceiver>>();

	protected final LinkedHashMap<String, SensorBridge> nameToSensorBridgeMap = new LinkedHashMap<String, SensorBridge>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public ISensor lookup(String sensorName)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			SensorBridge sensor = nameToSensorBridgeMap.get(sensorName);
			if (sensor != null)
			{
				return sensor;
			}
			List<ISensorReceiver> sensorList = nameToSensorsMap.get(sensorName);
			if (sensorList == null)
			{
				sensorList = new ArrayList<ISensorReceiver>();
				nameToSensorsMap.put(sensorName, sensorList);
			}
			Lock bridgeLock = new ReentrantLock();
			sensor = new SensorBridge(sensorName, sensorList, bridgeLock);
			nameToSensorBridgeMap.put(sensorName, sensor);
			return sensor;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void registerSensorReceiver(ISensorReceiver sensorReceiver, String sensorName)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			List<ISensorReceiver> sensorList = nameToSensorsMap.get(sensorName);
			if (sensorList == null)
			{
				sensorList = new ArrayList<ISensorReceiver>();
				nameToSensorsMap.put(sensorName, sensorList);
			}
			SensorBridge sensorBridge = nameToSensorBridgeMap.get(sensorName);
			if (sensorBridge == null)
			{
				sensorList.add(sensorReceiver);
				return;
			}
			Lock bridgeLock = sensorBridge.writeLock;
			bridgeLock.lock();
			try
			{
				sensorList.add(sensorReceiver);
			}
			finally
			{
				bridgeLock.unlock();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterSensorReceiver(ISensorReceiver sensorReceiver, String sensorName)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			List<ISensorReceiver> sensorList = nameToSensorsMap.get(sensorName);
			if (sensorList == null)
			{
				return;
			}
			SensorBridge sensorBridge = nameToSensorBridgeMap.get(sensorName);
			if (sensorBridge == null)
			{
				sensorList.remove(sensorReceiver);
			}
			else
			{
				Lock bridgeLock = sensorBridge.writeLock;
				bridgeLock.lock();
				try
				{
					sensorList.remove(sensorReceiver);
				}
				finally
				{
					bridgeLock.unlock();
				}
			}
			if (sensorList.size() == 0 && !nameToSensorBridgeMap.containsKey(sensorName))
			{
				nameToSensorsMap.remove(sensorName);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
