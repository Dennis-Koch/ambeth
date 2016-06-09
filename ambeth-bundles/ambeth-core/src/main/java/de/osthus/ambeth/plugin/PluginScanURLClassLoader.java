package de.osthus.ambeth.plugin;

import java.net.URL;
import java.net.URLClassLoader;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;

public class PluginScanURLClassLoader extends URLClassLoader implements IInitializingBean
{
	@Autowired
	protected IJarURLProvidable jarURLProvidable;

	public PluginScanURLClassLoader()
	{
		super(new URL[0]);
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		for (URL url : jarURLProvidable.getJarURLs())
		{
			this.addURL(url);
		}
	}
}
