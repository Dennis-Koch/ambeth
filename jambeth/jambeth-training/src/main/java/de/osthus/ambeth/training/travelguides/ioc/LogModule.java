package de.osthus.ambeth.training.travelguides.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.training.travelguides.proxy.LogCallsPostProcessor;

@FrameworkModule
public class LogModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{

		bcf.registerBean(LogCallsPostProcessor.class);

	}
}
