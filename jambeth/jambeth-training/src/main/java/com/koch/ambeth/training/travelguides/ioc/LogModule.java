package com.koch.ambeth.training.travelguides.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.training.travelguides.proxy.LogCallsPostProcessor;

@FrameworkModule
public class LogModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{

		bcf.registerBean(LogCallsPostProcessor.class);

	}
}
