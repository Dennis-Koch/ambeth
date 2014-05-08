package de.osthus.ambeth.sensor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class SensorPreProcessor extends SmartCopyMap<Class<?>, Object[]> implements IBeanPreProcessor, IInitializingBean
{
	protected ISensorProvider sensorProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(sensorProvider, "SensorProvider");
	}

	public void setSensorProvider(ISensorProvider sensorProvider)
	{
		this.sensorProvider = sensorProvider;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
	{
		ISensorProvider sensorProvider = this.sensorProvider;
		Object[] sensorFields = getSensorFields(beanType);
		Field[] relevantFields = (Field[]) sensorFields[0];
		String[] sensorNames = (String[]) sensorFields[1];
		for (int a = relevantFields.length; a-- > 0;)
		{
			ISensor sensor = sensorProvider.lookup(sensorNames[a]);
			if (sensor == null)
			{
				continue;
			}
			try
			{
				relevantFields[a].set(service, sensor);
			}
			catch (IllegalAccessException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		for (IPropertyInfo prop : properties)
		{
			if (!prop.isWritable())
			{
				continue;
			}
			Sensor sensorAttribute = prop.getAnnotation(Sensor.class);
			if (sensorAttribute == null)
			{
				continue;
			}
			String sensorName = sensorAttribute.name();
			ISensor sensor = sensorProvider.lookup(sensorName);
			if (sensor == null)
			{
				continue;
			}
			prop.setValue(service, sensor);
		}
	}

	protected Object[] getSensorFields(Class<?> type)
	{
		Object[] sensorFields = get(type);
		if (sensorFields != null)
		{
			return sensorFields;
		}
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			sensorFields = get(type);
			if (sensorFields != null)
			{
				// Concurrent thread might have been faster
				return sensorFields;
			}
			ArrayList<Field> targetFields = new ArrayList<Field>();
			ArrayList<String> targetSensorNames = new ArrayList<String>();
			Class<?> currType = type;
			while (currType != Object.class && currType != null)
			{
				Field[] fields = ReflectUtil.getDeclaredFields(currType);
				for (Field field : fields)
				{
					int modifiers = field.getModifiers();
					if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers))
					{
						continue;
					}
					Sensor sensorAttribute = field.getAnnotation(Sensor.class);
					if (sensorAttribute == null)
					{
						continue;
					}
					targetFields.add(field);
					targetSensorNames.add(sensorAttribute.name());
				}
				currType = currType.getSuperclass();
			}
			sensorFields = new Object[] { targetFields.toArray(Field.class), targetSensorNames.toArray(String.class) };
			put(type, sensorFields);
			return sensorFields;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
