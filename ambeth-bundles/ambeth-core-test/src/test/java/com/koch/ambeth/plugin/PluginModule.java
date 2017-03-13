package com.koch.ambeth.plugin;

import com.koch.ambeth.core.plugin.IJarURLProvider;
import com.koch.ambeth.core.plugin.JarURLProvider;
import com.koch.ambeth.core.plugin.PluginClasspathScanner;
import com.koch.ambeth.core.start.IClasspathInfo;
import com.koch.ambeth.core.start.SystemClasspathInfo;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class PluginModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{
		bcf.registerBean(SystemClasspathInfo.class).autowireable(IClasspathInfo.class);

		bcf.registerBean(JarURLProvider.class).autowireable(IJarURLProvider.class);

		bcf.registerBean(PluginClasspathScanner.class).autowireable(PluginClasspathScanner.class);
	}
}
