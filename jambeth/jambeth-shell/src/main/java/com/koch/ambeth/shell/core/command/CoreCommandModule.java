package com.koch.ambeth.shell.core.command;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class CoreCommandModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{
		bcf.registerBean(CoreCommandBundle.class);
	}

}
