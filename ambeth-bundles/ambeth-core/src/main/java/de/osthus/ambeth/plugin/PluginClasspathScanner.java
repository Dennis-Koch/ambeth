package de.osthus.ambeth.plugin;

import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.start.CoreClasspathScanner;

public class PluginClasspathScanner extends CoreClasspathScanner implements IPluginClasspathScanner
{

	@Property(name = CoreConfigurationConstants.ClasspathPluginPath)
	protected String classScanPaths;

	@Autowired
	protected PluginScanClassPool classPool;

	@Autowired
	protected URLClassLoader urlClassLoader;

	@Autowired
	protected IJarURLProvidable jarURLProvidable;

	@Override
	protected ClassPool getClassPool()
	{
		return classPool;
	}

	@Override
	protected ClassLoader getClassLoader()
	{
		return urlClassLoader;
	}

	@Override
	protected ArrayList<URL> getJarURLs()
	{
		return jarURLProvidable.getJarURLs();
	}
}
