package de.osthus.ambeth.shell.core.command;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class CoreCommandModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{
		bcf.registerBean(CoreCommandBundle.class);
	}

}
