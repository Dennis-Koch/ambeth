package de.osthus.ambeth.start;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SystemClasspathInfo implements IClasspathInfo
{
	@LogInstance
	private ILogger log;

	@Override
	public IList<URL> getJarURLs()
	{
		ArrayList<URL> urls = new ArrayList<URL>();

		String cpString = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String[] cpItems = cpString.split(Pattern.quote(separator));
		for (int a = 0, size = cpItems.length; a < size; a++)
		{
			try
			{
				URL url = new URL("file://" + cpItems[a]);
				urls.add(url);
			}
			catch (MalformedURLException e)
			{
				throw RuntimeExceptionUtil.mask(e, cpItems[a]);
			}
		}

		return urls;
	}

	@Override
	public Path openAsFile(URL url) throws Throwable
	{
		return Paths.get(new URL(url.getProtocol(), url.getHost(), url.getFile().replace(" ", "%20")).toURI()).toAbsolutePath().normalize();
	}
}
