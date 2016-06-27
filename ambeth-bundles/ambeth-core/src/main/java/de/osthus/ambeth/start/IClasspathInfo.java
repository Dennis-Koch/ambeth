package de.osthus.ambeth.start;

import java.net.URL;
import java.nio.file.Path;

import de.osthus.ambeth.collections.IList;

public interface IClasspathInfo
{
	IList<URL> getJarURLs();

	Path openAsFile(URL url) throws Throwable;
}
