package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.sensor.ISensorProvider;
import de.osthus.ambeth.sensor.ISensorReceiverExtendable;
import de.osthus.ambeth.sensor.SensorPreProcessor;
import de.osthus.ambeth.sensor.SensorProvider;

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
