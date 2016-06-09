package de.osthus.ambeth.plugin;

import java.net.URL;
import java.net.URLClassLoader;

public class URLAddableURLClassLoader extends URLClassLoader
{
	public URLAddableURLClassLoader()
	{
		super(new URL[0]);
	}

	@Override
	public void addURL(URL url)
	{
		super.addURL(url);
	}
}
