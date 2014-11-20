package de.osthus.ambeth.demo;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TestService1 implements ITestInterface
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void testMethod1()
	{
		String str1 = "Hello";
		String str2 = "World";
		String msg = str1 + testMethod3() + str2;
		System.out.println(msg);
	}

	@Override
	public int testMethod2()
	{
		int value;
		value = 0;
		// Test comment
		for (int i = 0; i < 11; i++)
		{
			value += i;
		}
		return value;
	}

	@Override
	public String testMethod3()
	{
		return " ";
	}

	@Override
	public void testMethod4(String stringParam, int count)
	{
		String[] data = new String[count];
		for (int i = 0; i < count; i++)
		{
			data[i] = stringParam;
		}
		StringBuilder sb = new StringBuilder();
		for (String str : data)
		{
			sb.append(str);
		}
		String value = sb.toString();
		System.out.println(value);
	}
}
