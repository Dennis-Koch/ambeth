package de.osthus.ambeth.demo;

import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TestService1 extends BaseClass implements ITestInterface
{
	private static final String WORLD = "World";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected int magicNumber = 42;

	protected String magicString = magicNumber + "";

	@Override
	public void testMethod1()
	{
		magicNumber = Integer.parseInt(magicString);

		final String str1 = hello;
		String str2 = WORLD;
		String msg = "";
		try
		{
			msg = str1 + testMethod3() + str2;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			System.out.println(msg);
		}
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

	protected void testMethod5()
	{
		long millis = System.currentTimeMillis();
		int rest = (int) (millis % 3);
		switch (rest)
		{
			case 1:
			{
				System.out.println("1");
				break;
			}
			// Intentionally not a block
			case 2:
				System.out.println("2");
				break;
			default:
			{
				System.out.println("ok");
				break;
			}
		}
	}

	@Override
	public void overridableMethod()
	{
		System.out.println("call me");
	}

	public void callAnotherSuperMethod()
	{
		overridableMethod();
		this.overridableMethod();
		super.overridableMethod();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public void toString(StringBuilder sb)
	{
		sb.append(TestService1.class.getSimpleName());
	}
}
