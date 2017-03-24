package com.koch.ambeth.xml.util;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.koch.ambeth.xml.config.XmlConfigurationConstants;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class ClasspathScanner implements IClasspathScanner {
	@LogInstance
	private ILogger log;

	public static final Pattern jarPathPrefixPattern = Pattern.compile("(.+)/([^/]+)");

	public static final Pattern cutDollarPattern =
			Pattern.compile("([^\\$\\.]+)(?:\\$[\\.]+)?\\.(?:java|class)");

	public static final Pattern subPathPattern = Pattern.compile("(/[^/]+)?(/.+)");

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IObjectCollector objectCollector;

	@Autowired(optional = true)
	protected IClasspathScannerServletContext classpathScannerServletContext;

	@Property(name = XmlConfigurationConstants.PackageScanPatterns, defaultValue = "com/koch/.*")
	protected String packageFilterPatterns;

	protected Pattern[] packageScanPatterns;

	protected Pattern[] preceedingPackageScanPatterns;

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
		ClassPool pool = ClassPool.getDefault();
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
		ClassPool pool = ClassPool.getDefault();
		IList<String> targetClassNames = scanForClasses(pool);
		try {
			CtClass[] ctSuperTypes = new CtClass[superTypes.length];
			for (int a = superTypes.length; a-- > 0;) {
				ctSuperTypes[a] = pool.getCtClass(superTypes[a].getName());
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

	protected List<Class<?>> convertToClasses(List<CtClass> ctClasses) {
		HashSet<Class<?>> set = new HashSet<>();
		for (int a = 0, size = ctClasses.size(); a < size; a++) {
			CtClass ctClass = ctClasses.get(a);
			try {
				set.add(classLoaderProvider.getClassLoader().loadClass(ctClass.getName()));
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Class<?>> list = new ArrayList<>(set.size());
		set.toList(list);
		return list;
	}

	protected IList<String> scanForClasses(ClassPool pool) {
		IList<URL> urls = buildUrlsFromClasspath(pool);

		ArrayList<String> namespacePatterns = new ArrayList<>();
		ArrayList<String> targetClassNames = new ArrayList<>();

		try {
			for (int a = 0, size = urls.size(); a < size; a++) {
				URL url = urls.get(a);
				String path = url.getPath();

				try {
					path = classpathScannerServletContext != null
							? classpathScannerServletContext.lookupExistingPath(path) : path;
					File realPathFile = new File(path);
					pool.appendPathList(path);
					if (realPathFile.isFile()) {
						JarFile jarFile = new JarFile(realPathFile);
						try {
							scanJarFile(jarFile, namespacePatterns, targetClassNames);
						}
						finally {
							jarFile.close();
						}
						continue;
					}
					else if (realPathFile.isDirectory()) {
						scanDirectory(realPathFile, "", targetClassNames, false);
					}
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e,
							"Error occured while handling URL '" + url.getPath() + "'");
				}
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return targetClassNames;
	}

	protected void scanJarFile(JarFile jarFile, List<String> namespacePatterns,
			List<String> targetClassNames) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
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
			tlObjectCollector.dispose(sb);
		}
	}

	protected void scanDirectory(File dir, String relativePath, List<String> targetClassNames,
			boolean addOnly) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try {
			File[] files = dir.listFiles();
			if (files == null) {
				throw new IllegalStateException("Directory '" + dir.getAbsolutePath() + "' not accessible");
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

	protected IList<URL> buildUrlsFromClasspath(ClassPool pool) {

		if (classpathScannerServletContext != null) {
			return classpathScannerServletContext.buildUrlsFromClasspath(pool);
		}
		ArrayList<URL> urls = new ArrayList<>();
		String cpString = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String[] cpItems = cpString.split(Pattern.quote(separator));
		for (int a = 0, size = cpItems.length; a < size; a++) {
			try {
				URL url = new URL("file://" + cpItems[a]);
				urls.add(url);
			}
			catch (MalformedURLException e) {
				throw RuntimeExceptionUtil.mask(e, cpItems[a]);
			}
		}
		return urls;
	}

	protected String buildPatternFailMessage(Pattern pattern, String value) {
		return "Matcher should have matched: Pattern: '" + pattern.pattern() + "'. Value '" + value
				+ "'";
	}
}
