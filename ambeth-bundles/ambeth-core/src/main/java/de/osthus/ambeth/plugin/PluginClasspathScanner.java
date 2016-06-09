package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javassist.ClassPool;
import javassist.NotFoundException;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.start.CoreClasspathScanner;
import de.osthus.ambeth.util.ParamChecker;

public class PluginClasspathScanner extends CoreClasspathScanner implements IPluginClasspathScanner, IInitializingBean
{

	@Property(name = CoreConfigurationConstants.ClasspathPluginPath)
	protected String classScanPaths;

	@Autowired
	protected ClassPool classPool;
	@Autowired
	protected URLAddableURLClassLoader urlClassLoader;

	protected ArrayList<URL> jarURLs;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		jarURLs = new ArrayList<URL>();
		this.initClassScanPath(classScanPaths.split(";"));
	}

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
		return jarURLs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.start.IPluginClasspathScanner#setClassScanPath(java.lang.String)
	 */
	private void initClassScanPath(String... paths) throws NotFoundException
	{
		for (String path : paths)
		{
			classPool.appendClassPath(path);
		}
		List<URL> extractedjarURLs = extractJarURL(paths);
		jarURLs.addAll(extractedjarURLs);
		for (URL url : jarURLs)
		{
			urlClassLoader.addURL(url);
		}
	}

	private List<URL> extractJarURL(String... pathArray)
	{
		List<URL> urls = new ArrayList<URL>();
		for (String path : pathArray)
		{
			urls.addAll(buildUrl(path));
		}
		return urls;
	}

	private static List<URL> buildUrl(String path)
	{
		List<URL> urls = new ArrayList<URL>();
		File dir = null;
		try
		{
			if (path.toLowerCase().endsWith(".jar"))
			{
				urls.add(new URL("file:" + path));
			}
			else if ((dir = new File(path)).isDirectory())
			{
				urls.add(dir.toURI().toURL());
			}
			else if (path.matches(".*(\\\\{1,2}|/)\\*+"))
			{
				String newPath = path.replaceFirst("(\\\\{1,2}|/)\\*+$", "");
				File trimEndPath = new File(newPath);
				ParamChecker.assertTrue(trimEndPath.isDirectory(), newPath);
				urls.addAll(findAllJarFiles(trimEndPath));
			}
			return urls;
		}
		catch (MalformedURLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private static List<URL> findAllJarFiles(File path) throws MalformedURLException
	{
		List<URL> files = new ArrayList<URL>();
		if (path.isFile() && path.getName().toLowerCase().endsWith(".jar"))
		{
			files.add(path.toURI().toURL());
		}
		else if (path.isDirectory())
		{
			File[] subFiles = path.listFiles();
			for (File subFile : subFiles)
			{
				files.addAll(findAllJarFiles(subFile));
			}
		}
		return files;
	}
}
