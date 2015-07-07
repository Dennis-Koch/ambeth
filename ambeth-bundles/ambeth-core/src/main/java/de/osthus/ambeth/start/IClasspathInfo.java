package de.osthus.ambeth.start;

import java.io.File;
import java.net.URL;

import de.osthus.ambeth.collections.ArrayList;

public interface IClasspathInfo
{
	ArrayList<URL> getJarURLs();

	File openAsFile(URL url) throws Throwable;
}
