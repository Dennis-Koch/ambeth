package com.koch.ambeth.core.plugin;

import java.net.URL;
import java.net.URLClassLoader;

import com.koch.ambeth.core.start.CoreClasspathScanner;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.IList;

import javassist.ClassPool;

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
