package de.osthus.ambeth.demo;

import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TestService1 extends BaseClass implements ITestInterface
{
	private static final String WORLD = "World";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void testMethod1()
	{
		String str1 = hello;
		String str2 = WORLD;
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
		Object string = " ";
		return (String) string;
	}

	@Override
	public void testMethod4(String stringParam, int count)
	{
		String[] data = new String[count];
		for (int i = 0, length = data.length; i < length; i++)
		{
			data[i] = stringParam;
		}

		StringBuilder sb = new StringBuilder();
		for (String str : data)
		{
			sb.append(str);
		}

		List<String> strings = Arrays.asList(data);
		for (String str : strings)
		{
			sb.append(str);
		}

		String value = sb.toString();
		System.out.println(value);
	}
}
