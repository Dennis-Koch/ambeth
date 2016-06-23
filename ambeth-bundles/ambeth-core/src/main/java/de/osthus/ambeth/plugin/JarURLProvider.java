package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;

public class JarURLProvider implements IJarURLProvider, IInitializingBean
{
	@Property(name = CoreConfigurationConstants.PluginPaths)
	protected String[] jarPaths;

	protected ArrayList<URL> jarURLs;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		jarURLs = extractJarURL(jarPaths);
	}

	@Override
	public IList<URL> getJarURLs()
	{
		return jarURLs;
	}

	private ArrayList<URL> extractJarURL(String... pathArray)
	{
		ArrayList<URL> urls = new ArrayList<URL>();
		for (String path : pathArray)
		{
			urls.addAll(buildUrl(path));
		}
		return urls;
	}

	private List<URL> buildUrl(String path)
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
				urls.addAll(listJars(dir));
			}
			else if (path.endsWith("\\*") || path.endsWith("/*"))
			{
				String trimedWildcardPath = path.substring(0, path.length() - 2);
				File trimWildcardPath = new File(trimedWildcardPath);
				ParamChecker.assertTrue(trimWildcardPath.isDirectory(), trimedWildcardPath);
				urls.addAll(listAllJars(trimWildcardPath));
			}
			else
			{
				throw new IllegalArgumentException("path for scan plugin is not right format or the file does not exist: '" + path + "'");
			}
			return urls;
		}
		catch (MalformedURLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private List<URL> listAllJars(File dir) throws MalformedURLException
	{
		List<URL> files = new ArrayList<URL>();
		if (dir.isFile() && dir.getName().toLowerCase().endsWith(".jar"))
		{
			files.add(dir.toURI().toURL());
		}
		else if (dir.isDirectory())
		{
			File[] subFiles = dir.listFiles();
			for (File subFile : subFiles)
			{
				files.addAll(listAllJars(subFile));
			}
		}
		return files;
	}

	private List<URL> listJars(File dir) throws MalformedURLException
	{
		File[] listFiles = dir.listFiles();
		List<URL> result = new ArrayList<URL>();
		for (File file : listFiles)
		{
			if (file.isFile() && file.getName().toLowerCase().endsWith(".jar"))
			{
				result.add(file.toURI().toURL());
			}
		}
		return result;
	}

}
