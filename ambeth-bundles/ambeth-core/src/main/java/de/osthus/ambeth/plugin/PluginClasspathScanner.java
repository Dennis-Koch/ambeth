package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.start.CoreClasspathScanner;

public class PluginClasspathScanner extends CoreClasspathScanner implements IPluginClasspathScanner
{
	@Autowired
	protected URLClassLoader urlClassLoader;

	@Autowired
	protected IJarURLProvidable jarURLProvidable;

	protected ClassPool classPool = new ClassPool(false);

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

	@Override
	protected File convertURLToFile(URL url) throws Throwable
	{
		return new File(url.getPath());
	}
}
