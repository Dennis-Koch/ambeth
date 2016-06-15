package de.osthus.ambeth.plugin;

import java.net.URLClassLoader;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.start.IClasspathInfo;
import de.osthus.ambeth.start.SystemClasspathInfo;

public class PluginModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{

		bcf.registerBean(SystemClasspathInfo.class).autowireable(IClasspathInfo.class);

		bcf.registerBean(JarURLProvider.class).autowireable(IJarURLProvider.class);

		bcf.registerBean(PluginScanURLClassLoader.class).autowireable(PluginScanURLClassLoader.class, URLClassLoader.class);
		bcf.registerBean(PluginClasspathScanner.class).autowireable(IPluginClasspathScanner.class);
	}
}
