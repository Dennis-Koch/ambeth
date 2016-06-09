package de.osthus.ambeth.plugin;

import javassist.ClassPool;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;

public class PluginScanClassPool extends ClassPool implements IInitializingBean
{
	@Autowired
	protected IJarURLProvidable jarURLProvidable;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		for (String path : jarURLProvidable.getJarPaths())
		{
			this.appendClassPath(path);
		}
	}
}
