package com.koch.ambeth.sensor.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.sensor.SensorPreProcessor;
import com.koch.ambeth.sensor.SensorProvider;
import com.koch.ambeth.util.sensor.ISensorProvider;
import com.koch.ambeth.util.sensor.ISensorReceiverExtendable;

@FrameworkModule
public class SensorModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		Object sensorProvider = beanContextFactory.registerBean("sensorProvider", SensorProvider.class)
				.autowireable(ISensorProvider.class, ISensorReceiverExtendable.class).precedence(PrecedenceType.HIGHEST).getInstance();
		beanContextFactory.registerBean(SensorPreProcessor.class).propertyValue("SensorProvider", sensorProvider);
	}
}
