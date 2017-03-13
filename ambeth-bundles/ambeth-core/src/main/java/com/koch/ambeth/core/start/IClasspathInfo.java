package com.koch.ambeth.core.start;

import java.net.URL;
import java.nio.file.Path;

import com.koch.ambeth.util.collections.IList;

public interface IClasspathInfo
{
	IList<URL> getJarURLs();

	Path openAsFile(URL url) throws Throwable;
}
