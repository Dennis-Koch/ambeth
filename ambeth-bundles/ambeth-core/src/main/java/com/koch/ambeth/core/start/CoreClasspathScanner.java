package com.koch.ambeth.core.start;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class CoreClasspathScanner implements IClasspathScanner, IInitializingBean {
	@LogInstance
	private ILogger log;

	public static final Pattern jarPathPrefixPattern = Pattern.compile("(.+)/([^/]+)");

	public static final Pattern cutDollarPattern =
			Pattern.compile("([^\\$\\.]+)(?:\\$[\\.]+)?\\.(?:java|class)");

	@Autowired
	protected IClasspathInfo classpathInfo;

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IObjectCollector objectCollector;

	@Property(name = CoreConfigurationConstants.PackageScanPatterns, defaultValue = "com/koch/.*")
	protected String packageFilterPatterns;

	protected Pattern[] packageScanPatterns;

	protected Pattern[] preceedingPackageScanPatterns;

	protected ClassPool classPool;

	@Override
	public void afterPropertiesSet() throws Throwable {
		// intended blank
	}

	protected void initializeClassPool(ClassPool classPool) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing ClassPool:");
		}

		for (URL url : getJarURLs()) {
			try {
				String pathList = convertURLToFile(url).toString();
				if (log.isDebugEnabled()) {
					log.debug("Append URL to path: " + url + " (converted file path: " + pathList + ")");
				}
				classPool.appendPathList(pathList);
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	// Has to be used by getter since it might be needed before afterPropertiesSet() was called.
	protected Pattern[] getPackageScanPatterns() {
		if (packageScanPatterns == null) {
			ParamChecker.assertNotNull(packageFilterPatterns, "packageFilterPatterns");

			String[] split = packageFilterPatterns.split(";");
			ArrayList<Pattern> patterns = new ArrayList<>();
			for (int a = split.length; a-- > 0;) {
				String packagePattern = split[a];
				String packagePattern1 =
						packagePattern.replaceAll(Pattern.quote("\\."), Matcher.quoteReplacement("/"));
				patterns.add(Pattern.compile(packagePattern));
				if (!packagePattern1.equals(packagePattern)) {
					patterns.add(Pattern.compile(packagePattern1));
				}
			}
			packageScanPatterns = patterns.toArray(Pattern.class);
		}
		return packageScanPatterns;
	}

	@Override
	public List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes) {
		ClassPool pool = getClassPool();
		IList<String> targetClassNames = scanForClasses(pool);
		try {
			List<CtClass> classNamesFound = new ArrayList<>();
			for (int a = 0, size = targetClassNames.size(); a < size; a++) {
				String className = targetClassNames.get(a);
				CtClass cc;
				try {
					cc = pool.get(className);
				}
				catch (NotFoundException e) {
					if (log.isErrorEnabled()) {
						log.error("Javassist could not load class (but found it in classpath): " + className);
					}
					continue;
				}
				for (int b = annotationTypes.length; b-- > 0;) {
					Object annotation = cc.getAnnotation(annotationTypes[b]);

					if (annotation == null) {
						continue;
					}
					classNamesFound.add(cc);
					break;
				}
			}
			return convertToClasses(classNamesFound);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public List<Class<?>> scanClassesImplementing(Class<?>... superTypes) {
		ClassPool pool = getClassPool();
		IList<String> targetClassNames = scanForClasses(pool);
		try {
			CtClass[] ctSuperTypes = new CtClass[superTypes.length];
			for (int a = superTypes.length; a-- > 0;) {
				ctSuperTypes[a] = pool.getOrNull(superTypes[a].getName());
				if (ctSuperTypes[a] == null) {
					ctSuperTypes[a] = ClassPool.getDefault().get(superTypes[a].getName());
				}
			}
			List<CtClass> classNamesFound = new ArrayList<>();
			for (int a = 0, size = targetClassNames.size(); a < size; a++) {
				String className = targetClassNames.get(a);
				CtClass cc = pool.get(className);
				for (int b = ctSuperTypes.length; b-- > 0;) {
					if (!cc.subtypeOf(ctSuperTypes[b])) {
						continue;
					}
					classNamesFound.add(cc);
				}
			}
			return convertToClasses(classNamesFound);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected ClassPool getClassPool() {
		if (classPool == null) {
			classPool = ClassPool.getDefault();
			initializeClassPool(classPool);
		}
		return classPool;
	}

	protected List<Class<?>> convertToClasses(List<CtClass> ctClasses) {
		HashSet<Class<?>> set = new HashSet<>();
		for (int a = 0, size = ctClasses.size(); a < size; a++) {
			CtClass ctClass = ctClasses.get(a);
			try {
				set.add(getClassLoader().loadClass(ctClass.getName()));
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Class<?>> list = new ArrayList<>(set.size());
		set.toList(list);
		return list;
	}

	protected ClassLoader getClassLoader() {
		return classLoaderProvider.getClassLoader();
	}

	protected IList<String> scanForClasses(ClassPool pool) {
		IList<URL> urls = getJarURLs();

		ArrayList<String> targetClassNames = new ArrayList<>();

		try {
			for (int a = 0, size = urls.size(); a < size; a++) {
				URL url = urls.get(a);
				try {
					Path realPathFile = convertURLToFile(url);
					if (realPathFile.toFile().exists()) {
						if (Files.isDirectory(realPathFile)) {
							scanDirectory(realPathFile, "", targetClassNames, false);
						}
						else {
							scanFile(realPathFile, targetClassNames);
						}
					}
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e,
							"Error occured while handling URL '" + url.toString() + "'");
				}
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return targetClassNames;
	}

	protected IList<URL> getJarURLs() {
		return classpathInfo.getJarURLs();
	}

	protected Path convertURLToFile(URL url) throws Throwable {
		return classpathInfo.openAsFile(url);
	}

	protected void scanFile(Path file, List<String> targetClassNames) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try {
			JarFile jarFile = new JarFile(file.toFile());
			try {
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.isDirectory()) {
						continue;
					}
					String entryName = entry.getName();

					Pattern[] packageScanPatterns = getPackageScanPatterns();
					for (int a = packageScanPatterns.length; a-- > 0;) {
						Matcher pathMatcher = packageScanPatterns[a].matcher(entryName);
						if (!pathMatcher.matches()) {
							continue;
						}
						Matcher matcher = jarPathPrefixPattern.matcher(entryName);
						String path, name;
						if (!matcher.matches()) {
							path = "";
							name = entryName;
						}
						else {
							path = matcher.group(1);
							name = matcher.group(2);
						}
						Matcher cutDollarMatcher = cutDollarPattern.matcher(name);
						if (!cutDollarMatcher.matches()) {
							continue;
						}
						sb.setLength(0);
						sb.append(path);
						if (path.length() > 0) {
							sb.append('/');
						}
						sb.append(cutDollarMatcher.group(1));
						String className = StringBuilderUtil.replace(sb, '/', '.').toString();
						targetClassNames.add(className);
					}
				}
			}
			finally {
				jarFile.close();
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			tlObjectCollector.dispose(sb);
		}
	}

	protected void scanDirectory(Path dir, String relativePath, List<String> targetClassNames,
			boolean addOnly) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try {
			File[] files = dir.toFile().listFiles();
			if (files == null) {
				throw new IllegalStateException(
						"Directory '" + dir.toFile().getAbsolutePath() + "' not accessible");
			}
			sb.append(relativePath);
			if (relativePath.length() > 0) {
				sb.append('/');
			}
			int sbStartLength = sb.length();

			for (int a = 0, size = files.length; a < size; a++) {
				File file = files[a];

				sb.setLength(sbStartLength);
				if (file.isDirectory()) {
					sb.append(file.getName());
					String classNamePart = sb.toString();
					scanDirectory(file.toPath(), classNamePart, targetClassNames, addOnly);
					continue;
				}
				Matcher cutDollarMatcher = cutDollarPattern.matcher(file.getName());
				if (!cutDollarMatcher.matches()) {
					continue;
				}
				sb.append(cutDollarMatcher.group(1));
				String className = sb.toString();
				if (addOnly) {
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
				for (int b = packageScanPatterns.length; b-- > 0;) {
					Matcher pathMatcher = packageScanPatterns[b].matcher(className);
					if (pathMatcher.matches()) {
						targetClassNames.add(className.replace('/', '.'));
						break;
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
		finally {
			tlObjectCollector.dispose(sb);
		}
	}
}
