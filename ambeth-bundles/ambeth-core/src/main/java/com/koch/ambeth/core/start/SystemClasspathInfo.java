package com.koch.ambeth.core.start;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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

		if (log != null && log.isDebugEnabled())
		{
			log.debug("Classpath: " + cpString);
		}

		for (int a = 0, size = cpItems.length; a < size; a++)
		{
			try
			{
				URL url = new File(cpItems[a]).toURI().toURL();
				// URL url = new URL("file://" + cpItems[a]);
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
		return new File(url.getFile()).getAbsoluteFile().toPath();
	}
}
