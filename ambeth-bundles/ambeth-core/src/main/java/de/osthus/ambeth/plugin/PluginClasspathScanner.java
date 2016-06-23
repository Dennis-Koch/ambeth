package de.osthus.ambeth.plugin;

import java.net.URL;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.start.CoreClasspathScanner;

public class PluginClasspathScanner extends CoreClasspathScanner implements IPluginClasspathScanner
{
	@Autowired
	protected IJarURLProvider jarURLProvidable;

	@Autowired
	protected PluginScanURLClassLoader pluginScanURLClassLoader;

	@Override
	protected ClassLoader getClassLoader()
	{
		return pluginScanURLClassLoader;
	}

	@Override
	protected IList<URL> getJarURLs()
	{
		return jarURLProvidable.getJarURLs();
	}
}
