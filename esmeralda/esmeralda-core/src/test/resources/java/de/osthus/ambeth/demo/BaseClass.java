package de.osthus.ambeth.demo;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class BaseClass
{
	protected static final String hello = "Hello";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public void testMethod1()
	{
		String str = "BaseClass Hello";
		System.out.println(str);
	}
}
