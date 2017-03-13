package com.koch.ambeth.core.plugin;

import java.net.URL;

import com.koch.ambeth.util.collections.IList;

public interface IJarURLProvider
{
	/**
	 * supply the URLs for scan and load Class in jar file
	 * 
	 * @return
	 */
	IList<URL> getJarURLs();
}
