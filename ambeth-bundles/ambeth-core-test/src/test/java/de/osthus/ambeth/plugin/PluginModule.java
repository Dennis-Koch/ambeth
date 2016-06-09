package de.osthus.ambeth.plugin;

import javassist.ClassPool;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.start.IClasspathInfo;
import de.osthus.ambeth.start.SystemClasspathInfo;

public class PluginModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{

		bcf.registerBean(SystemClasspathInfo.class).autowireable(IClasspathInfo.class);

		bcf.registerBean(URLAddableURLClassLoader.class).autowireable(URLAddableURLClassLoader.class);
		bcf.registerBean(ClassPool.class).autowireable(ClassPool.class);
		bcf.registerBean(PluginClasspathScanner.class).autowireable(IPluginClasspathScanner.class);
	}
}
