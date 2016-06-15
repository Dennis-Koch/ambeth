package de.osthus.ambeth.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;

public class JarURLProvider implements IJarURLProvider, IInitializingBean
{
	@Property(name = CoreConfigurationConstants.PluginPaths)
	protected String[] jarPaths;

	@Property(name = CoreConfigurationConstants.PluginPathsRecursiveFlag, defaultValue = "true")
	protected boolean isRecursive;

	protected ArrayList<URL> jarURLs;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		jarURLs = this.extractJarURL(jarPaths);
	}

	@Override
	public ArrayList<URL> getJarURLs()
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
		File dir = new File(path);
		try
		{
			if (dir.isFile() && path.toLowerCase().endsWith(".jar"))
			{
				urls.add(new URL("file:" + path));
			}
			else if (dir.isDirectory() && !isRecursive)
			{
				urls.addAll(listJars(dir));
			}
			else if (dir.isDirectory() && isRecursive)
			{
				urls.addAll(listAllJars(dir));
			}
			else
			{
				throw new IllegalArgumentException("path for scan plugin is not jar file or not exists, path:" + path);
			}
			return urls;
		}
		catch (MalformedURLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * find all the jar files in a folder
	 * 
	 * @param dir
	 *            to find jars folder
	 * @return all the jars
	 * @throws MalformedURLException
	 */
	private List<URL> listAllJars(File dir) throws MalformedURLException
	{
		final List<URL> files = new ArrayList<URL>();
		try
		{
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					if (checkJar(file.toFile()))
					{
						files.add(file.toUri().toURL());
					}
					return super.visitFile(file, attrs);
				}
			});
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return files;
	}

	private List<URL> listJars(File dir) throws MalformedURLException
	{
		File[] listFiles = dir.listFiles();
		List<URL> result = new ArrayList<URL>();
		for (File file : listFiles)
		{
			if (checkJar(dir))
			{
				result.add(file.toURI().toURL());
			}
		}
		return result;
	}

	private boolean checkJar(File file)
	{
		return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
	}
}
