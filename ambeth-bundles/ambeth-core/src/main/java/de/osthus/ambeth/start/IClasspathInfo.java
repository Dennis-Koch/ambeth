package de.osthus.ambeth.start;

import java.net.URL;

import de.osthus.ambeth.collections.ArrayList;

public interface IClasspathInfo
{
	ArrayList<URL> getJarURLs();

	String lookupExistingPath(String path) throws Throwable;
}
