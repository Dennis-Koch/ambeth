package com.koch.ambeth.server.start;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;

import com.koch.ambeth.core.start.IClasspathInfo;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ServletClasspathInfo implements IClasspathInfo {
	private static final Pattern subPathPattern = Pattern.compile("(/[^/]+)?(/.+)");

	@LogInstance
	private ILogger log;

	@Autowired
	protected ServletContext servletContext;

	@Override
	public ArrayList<URL> getJarURLs() {
		ArrayList<URL> urls = new ArrayList<URL>();

		String libs = "/WEB-INF/lib";
		Set<String> libJars = servletContext.getResourcePaths(libs);
		if (libJars != null) {
			for (String jar : libJars) {
				try {
					urls.add(servletContext.getResource(jar));
				}
				catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}

		String classes = "/WEB-INF/classes";
		Set<String> classesSet = servletContext.getResourcePaths(classes);
		if (classesSet != null) {
			for (String folderElement : classesSet) {
				try {
					// FIXME We have to call getResourcePaths() until we get a file and than getResource().
					// Then we have to determine what part of the path is the folder and what the package part
					// That way we find classes folders from different projects in Eclipse
					// There may be duplicates, so we have to use a Set for that.
					URL url = servletContext.getResource(folderElement);
					if (url != null) {
						if (url.toString().startsWith("file:/")) {
							File file = new File(url.toURI());
							if (file.isDirectory()) {
								urls.add(file.getParentFile().toURI().toURL());
							}
						}
						else if (url.toString().startsWith("jndi:/")) {
							Object content = url.getContent(new Class[] {DirContext.class});
							if (content != null && content instanceof DirContext) {
								DirContext dirContent = (DirContext) content;
								File file = new File(dirContent.getNameInNamespace());
								if (file.isDirectory()) {
									urls.add(file.getParentFile().toURI().toURL());
								}
							}
						}
					}
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return urls;
	}

	@Override
	public Path openAsFile(URL url) throws Throwable {
		String tempPath = url.getPath();
		while (true) {
			Matcher matcher = subPathPattern.matcher(tempPath);
			if (!matcher.matches()) {
				throw new IllegalStateException(buildPatternFailMessage(subPathPattern, tempPath));
			}
			tempPath = matcher.group(2);
			try {
				String realPath = servletContext.getRealPath(tempPath);
				if (url.toString().startsWith("file:/")) {
					File file = new File(url.toURI());
					if (file.isAbsolute()) {
						realPath = file.getAbsolutePath();
					}
				}

				// path has been handled correctly. check if it really exists
				File realPathFile = new File(realPath);
				if (!realPathFile.exists()) {
					// if (log.isWarnEnabled())
					// {
					// log.warn("Path '" + tempPath + "' does not exist!");
					// }
					throw new IllegalStateException(
							"Path '" + realPathFile.getAbsolutePath() + "' does not exist!");
				}
				return realPathFile.toPath();
			}
			catch (Throwable e) {
				if (matcher.group(1) == null || matcher.group(1).length() == 0) {
					// no prefix path anymore to potentially recover from this
					// failure
					throw e;
				}
				continue;
			}
		}
	}

	protected String buildPatternFailMessage(Pattern pattern, String value) {
		return "Matcher should have matched: Pattern: '" + pattern.pattern() + "'. Value '" + value
				+ "'";
	}
}
