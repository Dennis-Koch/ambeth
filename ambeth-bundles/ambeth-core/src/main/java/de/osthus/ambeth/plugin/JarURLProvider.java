package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;

public class JarURLProvider implements IJarURLProvidable, IInitializingBean
{
	@Property(name = CoreConfigurationConstants.ClasspathPluginPath)
	protected String classScanPaths;

	protected ArrayList<URL> jarURLs;

	protected String[] jarPaths;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		jarPaths = classScanPaths.split(";");
		jarURLs = this.extractJarURL(jarPaths);
	}

	@Override
	public ArrayList<URL> getJarURLs()
	{
		return jarURLs;
	}

	@Override
	public String[] getJarPaths()
	{
		return jarPaths;
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

	private List<URL> findAllJarFiles(File path) throws MalformedURLException
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
