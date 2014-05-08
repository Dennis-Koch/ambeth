package de.osthus.ambeth.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.servlet.ServletContext;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.XmlConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public class ClasspathScanner implements IInitializingBean, IClasspathScanner
{
	@LogInstance
	private ILogger log;

	public static final Pattern jarPathPrefixPattern = Pattern.compile("(.+)/([^/]+)");

	public static final Pattern cutDollarPattern = Pattern.compile("([^\\$\\.]+)(?:\\$[\\.]+)?\\.(?:java|class)");

	public static final Pattern subPathPattern = Pattern.compile("/[^/]+/[^/]+(/.+)");

	protected IObjectCollector objectCollector;

	protected ServletContext servletContext;

	protected String packageFilterPatterns;

	protected Pattern[] packageScanPatterns;

	protected Pattern[] preceedingPackageScanPatterns;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(packageFilterPatterns, "packageFilterPatterns");
	}

	public void setObjectCollector(IObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	@Property(name = XmlConfigurationConstants.PackageScanPatterns, defaultValue = "de/osthus.*")
	public void setPackageFilterPatterns(String packageFilterPatterns)
	{
		this.packageFilterPatterns = packageFilterPatterns;
	}

	// Has to be used by getter since it might be needed before afterPropertiesSet() was called.
	protected Pattern[] getPackageScanPatterns()
	{
		if (packageScanPatterns == null)
		{
			ParamChecker.assertNotNull(packageFilterPatterns, "packageFilterPatterns");

			String[] split = this.packageFilterPatterns.split(";");
			packageScanPatterns = new Pattern[split.length];
			for (int a = split.length; a-- > 0;)
			{
				String packagePattern = split[a];
				packageScanPatterns[a] = Pattern.compile(packagePattern);
			}
		}
		return packageScanPatterns;
	}

	@Override
	public List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes)
	{
		ClassPool pool = ClassPool.getDefault();
		IList<String> targetClassNames = scanForClasses(pool);
		try
		{
			List<CtClass> classNamesFound = new ArrayList<CtClass>();
			for (int a = 0, size = targetClassNames.size(); a < size; a++)
			{
				String className = targetClassNames.get(a);
				CtClass cc;
				try
				{
					cc = pool.get(className);
				}
				catch (NotFoundException e)
				{
					if (log.isErrorEnabled())
					{
						log.error("Javassist could not load class (but found it in classpath): " + className);
					}
					continue;
				}
				for (int b = annotationTypes.length; b-- > 0;)
				{
					Object annotation = cc.getAnnotation(annotationTypes[b]);

					if (annotation == null)
					{
						continue;
					}
					classNamesFound.add(cc);
					break;
				}
			}
			return convertToClasses(classNamesFound);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public List<Class<?>> scanClassesImplementing(Class<?>... superTypes)
	{
		ClassPool pool = ClassPool.getDefault();
		IList<String> targetClassNames = scanForClasses(pool);
		try
		{
			CtClass[] ctSuperTypes = new CtClass[superTypes.length];
			for (int a = superTypes.length; a-- > 0;)
			{
				ctSuperTypes[a] = pool.getCtClass(superTypes[a].getName());
			}
			List<CtClass> classNamesFound = new ArrayList<CtClass>();
			for (int a = 0, size = targetClassNames.size(); a < size; a++)
			{
				String className = targetClassNames.get(a);
				CtClass cc = pool.get(className);
				for (int b = ctSuperTypes.length; b-- > 0;)
				{
					if (!cc.subtypeOf(ctSuperTypes[b]))
					{
						continue;
					}
					classNamesFound.add(cc);
				}
			}
			return convertToClasses(classNamesFound);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected List<Class<?>> convertToClasses(List<CtClass> ctClasses)
	{
		HashSet<Class<?>> set = new HashSet<Class<?>>();
		for (int a = 0, size = ctClasses.size(); a < size; a++)
		{
			CtClass ctClass = ctClasses.get(a);
			try
			{
				set.add(Thread.currentThread().getContextClassLoader().loadClass(ctClass.getName()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Class<?>> list = new ArrayList<Class<?>>(set.size());
		set.toList(list);
		return list;
	}

	protected IList<String> scanForClasses(ClassPool pool)
	{
		List<URL> urls = new ArrayList<URL>();

		if (servletContext != null)
		{
			@SuppressWarnings("unchecked")
			Set<String> libJars = servletContext.getResourcePaths("/WEB-INF/lib");
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

			String classes = "/WEB-INF/classes";
			@SuppressWarnings("unchecked")
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
		}
		else
		{
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
		}

		List<String> namespacePatterns = new ArrayList<String>();
		IList<String> targetClassNames = new ArrayList<String>();

		for (int a = 0, size = urls.size(); a < size; a++)
		{
			URL url = urls.get(a);
			String path = url.getPath();

			if (servletContext != null)
			{
				Matcher matcher = subPathPattern.matcher(path);
				if (!matcher.matches())
				{
					throw new IllegalStateException(buildPatternFailMessage(subPathPattern, path));
				}
				path = matcher.group(1);
				path = servletContext.getRealPath(path);
			}
			File realPathFile = new File(path);
			try
			{
				pool.appendPathList(path);
			}
			catch (NotFoundException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			if (realPathFile.isFile())
			{
				JarFile jarFile;
				try
				{
					jarFile = new JarFile(realPathFile);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				scanJarFile(jarFile, namespacePatterns, targetClassNames);
				continue;
			}
			else
			{
				scanDirectory(realPathFile, "", targetClassNames, false);
			}
		}
		return targetClassNames;
	}

	protected void scanJarFile(JarFile jarFile, List<String> namespacePatterns, List<String> targetClassNames)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory())
				{
					continue;
				}
				String entryName = entry.getName();

				Pattern[] packageScanPatterns = getPackageScanPatterns();
				for (int a = packageScanPatterns.length; a-- > 0;)
				{
					Matcher pathMatcher = packageScanPatterns[a].matcher(entryName);
					if (!pathMatcher.matches())
					{
						continue;
					}
					Matcher matcher = jarPathPrefixPattern.matcher(entryName);
					String path, name;
					if (!matcher.matches())
					{
						path = "";
						name = entryName;
					}
					else
					{
						path = matcher.group(1);
						name = matcher.group(2);
					}
					Matcher cutDollarMatcher = cutDollarPattern.matcher(name);
					if (!cutDollarMatcher.matches())
					{
						continue;
					}
					sb.setLength(0);
					sb.append(path);
					if (path.length() > 0)
					{
						sb.append('/');
					}
					sb.append(cutDollarMatcher.group(1));
					String className = sb.toString();
					targetClassNames.add(className.replace('/', '.'));
				}
			}
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected void scanDirectory(File dir, String relativePath, List<String> targetClassNames, boolean addOnly)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			File[] files = dir.listFiles();
			if (files == null)
			{
				throw new IllegalStateException("Directory '" + dir.getAbsolutePath() + "' not accessable");
			}
			for (int a = 0, size = files.length; a < size; a++)
			{
				File file = files[a];

				sb.setLength(0);
				sb.append(relativePath);
				if (relativePath.length() > 0)
				{
					sb.append('/');
				}
				if (file.isDirectory())
				{
					sb.append(file.getName());
					String classNamePart = sb.toString();
					scanDirectory(file, classNamePart, targetClassNames, addOnly);
					// if (addOnly)
					// {
					// scanDirectory(file, classNamePart, null, targetClassNames, addOnly);
					// continue;
					// }
					//
					// for (int b = classPathItems.length; b-- > 0;)
					// {
					// ClassPathItem classPathItem = classPathItems[b];
					// Matcher matcher = classPathItem.getPattern().matcher(classNamePart);
					// if (!matcher.matches())
					// {
					// continue;
					// }
					// if (classPathItem.isIncludeThis())
					// {
					// scanDirectory(file, classNamePart, null, targetClassNames, true);
					// break;
					// }
					// }
					continue;
				}
				Matcher cutDollarMatcher = cutDollarPattern.matcher(file.getName());
				if (!cutDollarMatcher.matches())
				{
					continue;
				}
				sb.append(cutDollarMatcher.group(1));
				String className = sb.toString();
				if (addOnly)
				{
					targetClassNames.add(className.replace('/', '.'));
					continue;
				}
				// Matcher matcher = jarPathPrefixPattern.matcher(className);
				//
				// if (!matcher.matches())
				// {
				// continue;
				// }
				// String path = matcher.group(1);
				// String fileName = matcher.group(2);

				Pattern[] packageScanPatterns = getPackageScanPatterns();
				for (int b = packageScanPatterns.length; b-- > 0;)
				{
					Matcher pathMatcher = packageScanPatterns[b].matcher(className);
					if (pathMatcher.matches())
					{
						targetClassNames.add(className.replace('/', '.'));
					}
				}
				//
				// for (int b = classPathItems.length; b-- > 0;)
				// {
				// ClassPathItem classPathItem = classPathItems[b];
				// Matcher matcher = classPathItem.getPattern().matcher(className);
				// if (!matcher.matches())
				// {
				// continue;
				// }
				// targetClassNames.add(className);
				// }
			}
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected String buildPatternFailMessage(Pattern pattern, String value)
	{
		return "Matcher should have matched: Pattern: '" + pattern.pattern() + "'. Value '" + value + "'";
	}
}
