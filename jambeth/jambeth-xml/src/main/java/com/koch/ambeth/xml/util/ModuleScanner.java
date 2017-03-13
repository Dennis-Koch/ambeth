package com.koch.ambeth.xml.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.koch.ambeth.ioc.IModuleProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.collections.ArrayList;

public class ModuleScanner implements IModuleProvider
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IClasspathScanner classpathScanner;

	@Override
	public Class<?>[] getFrameworkModules()
	{
		return getModules(true);
	}

	@Override
	public Class<?>[] getBootstrapModules()
	{
		return getModules(false);
	}

	protected Class<?>[] getModules(boolean scanForFrameworkModule)
	{
		if (log.isInfoEnabled())
		{
			log.info("Looking for " + (scanForFrameworkModule ? "Ambeth" : "Application") + " bootstrap modules in classpath...");
		}
		List<Class<?>> bootstrapOrFrameworkModules = classpathScanner.scanClassesAnnotatedWith(scanForFrameworkModule ? FrameworkModule.class
				: BootstrapModule.class);

		List<Class<?>> bootstrapModules = new ArrayList<Class<?>>(bootstrapOrFrameworkModules.size());

		for (Class<?> bootstrapOrFrameworkModule : bootstrapOrFrameworkModules)
		{
			if (scanForFrameworkModule && bootstrapOrFrameworkModule.isAnnotationPresent(FrameworkModule.class))
			{
				bootstrapModules.add(bootstrapOrFrameworkModule);
			}
			else if (bootstrapOrFrameworkModule.isAnnotationPresent(BootstrapModule.class)
					&& !bootstrapOrFrameworkModule.isAnnotationPresent(FrameworkModule.class))
			{
				bootstrapModules.add(bootstrapOrFrameworkModule);
			}
		}
		if (log.isInfoEnabled())
		{
			log.info("Found " + bootstrapModules.size() + (scanForFrameworkModule ? " Ambeth" : " Application")
					+ " modules in classpath to include in bootstrap...");
			Collections.sort(bootstrapModules, new Comparator<Class<?>>()
			{
				@Override
				public int compare(Class<?> o1, Class<?> o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (int a = 0, size = bootstrapModules.size(); a < size; a++)
			{
				Class<?> boostrapModule = bootstrapModules.get(a);
				log.info("Including " + boostrapModule.getName());
			}
		}
		return bootstrapModules.toArray(new Class<?>[bootstrapModules.size()]);
	}
}
