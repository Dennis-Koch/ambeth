package de.osthus.ambeth.plugin;

import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.start.CoreClasspathScanner;

public class PluginClasspathScanner extends CoreClasspathScanner
{
	@Autowired
	protected IJarURLProvider jarURLProvider;

	protected URLClassLoader urlClassLoader;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		URL[] urls = jarURLProvider.getJarURLs().toArray(URL.class);
		urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
	}

	@Override
	public ClassLoader getClassLoader()
	{
		return urlClassLoader;
	}

	@Override
	protected IList<URL> getJarURLs()
	{
		return jarURLProvider.getJarURLs();
	}

	@Override
	protected ClassPool getClassPool()
	{
		if (classPool == null)
		{
			classPool = new ClassPool();
			initializeClassPool(classPool);
		}
		return classPool;
	}
}
