package de.osthus.ambeth.plugin;

import java.net.URLClassLoader;

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

		bcf.registerBean(JarURLProvider.class).autowireable(IJarURLProvidable.class);

		bcf.registerBean(PluginScanURLClassLoader.class).autowireable(PluginScanURLClassLoader.class);
		bcf.registerBean(PluginScanClassPool.class).autowireable(PluginScanClassPool.class);
		bcf.registerBean(PluginScanURLClassLoader.class).autowireable(URLClassLoader.class);
		bcf.registerBean(PluginClasspathScanner.class).autowireable(IPluginClasspathScanner.class);
	}
}
