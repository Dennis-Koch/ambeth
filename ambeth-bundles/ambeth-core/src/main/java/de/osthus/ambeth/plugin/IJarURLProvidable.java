package de.osthus.ambeth.plugin;

import java.net.URL;

import de.osthus.ambeth.collections.ArrayList;

public interface IJarURLProvidable
{
	/**
	 * supply the URLs for scan and load Class in jar file
	 * 
	 * @return
	 */
	ArrayList<URL> getJarURLs();

	/**
	 * supply the path for scan and load Class in jar file
	 * 
	 * @return
	 */
	String[] getJarPaths();
}
