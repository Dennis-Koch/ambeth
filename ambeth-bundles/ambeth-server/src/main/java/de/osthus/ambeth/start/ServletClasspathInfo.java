package de.osthus.ambeth.start;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ServletClasspathInfo implements IClasspathInfo
{
	private static final Pattern subPathPattern = Pattern.compile("(/[^/]+)?(/.+)");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ServletContext servletContext;

	@Override
	public ArrayList<URL> getJarURLs()
	{
		ArrayList<URL> urls = new ArrayList<URL>();

		String libs = "/WEB-INF/lib";
		Set<String> libJars = servletContext.getResourcePaths(libs);
		if (libJars != null && libJars.size() > 0)
		{
			for (String jar : libJars)
			{
				try
				{
					urls.add(servletContext.getResource(jar));
				}
				catch (MalformedURLException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		String classes = "/WEB-INF/classes";
		Set<String> classesSet = servletContext.getResourcePaths(classes);
		for (String jar : classesSet)
		{
			try
			{
				URL url = servletContext.getResource(jar);
				String urlString = url.toString();
				int index = urlString.lastIndexOf(classes);
				urlString = urlString.substring(0, index + classes.length());
				urls.add(new URL(urlString));
				break;
			}
			catch (MalformedURLException e)
			{
				throw new RuntimeException(e);
			}
		}

		return urls;
	}

	@Override
	public File openAsFile(URL url) throws Throwable
	{
		String tempPath = url.getPath();
		while (true)
		{
			Matcher matcher = subPathPattern.matcher(tempPath);
			if (!matcher.matches())
			{
				throw new IllegalStateException(buildPatternFailMessage(subPathPattern, tempPath));
			}
			tempPath = matcher.group(2);
			try
			{
				String realPath = servletContext.getRealPath(tempPath);
				// path has been handled correctly. check if it really exists
				File pathFile = new File(realPath);
				if (!pathFile.exists())
				{
					if (log.isWarnEnabled())
					{
						log.warn("Path '" + tempPath + "' does not exist!");
					}
					// throw new IllegalStateException("Path '" + realPath + "' does not exist!");
				}
				File file = new File(realPath);
				return file;
			}
			catch (Throwable e)
			{
				if (matcher.group(1) == null || matcher.group(1).length() == 0)
				{
					// no prefix path anymore to potentially recover from this failure
					throw e;
				}
				continue;
			}
		}
	}

	protected String buildPatternFailMessage(Pattern pattern, String value)
	{
		return "Matcher should have matched: Pattern: '" + pattern.pattern() + "'. Value '" + value + "'";
	}
}
