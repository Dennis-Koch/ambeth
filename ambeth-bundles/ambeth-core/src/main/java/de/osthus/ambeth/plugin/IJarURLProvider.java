package de.osthus.ambeth.plugin;

import java.net.URL;

import de.osthus.ambeth.collections.IList;

public interface IJarURLProvider
{
	/**
	 * supply the URLs for scan and load Class in jar file
	 * 
	 * @return
	 */
	IList<URL> getJarURLs();
}
