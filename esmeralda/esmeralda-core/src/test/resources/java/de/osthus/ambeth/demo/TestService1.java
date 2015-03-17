package de.osthus.ambeth.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TestService1 extends BaseClass implements ITestInterface
{
	private static final String WORLD = "World";

	public static String staticTestMethod()
	{
		staticTestMethod("world");
		return "";
	}

	public static void staticTestMethod(String name)
	{
		System.out.println("Hello " + name + "!");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected int magicNumber = 42;

	protected String magicString = magicNumber + "";

	protected TestService1 self = this;

	public TestService1()
	{
		new Object();
	}

	public TestService1(int magicNumber)
	{
		this.magicNumber = magicNumber;
	}

	@Override
	public void testMethod1()
	{
		magicNumber = Integer.parseInt(magicString);

		final String str1 = hello;
		String str2 = WORLD;
		String msg = "".toString().toString();
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
			String string1 = self.self.toString();
			TestService1 target = self.self.self;
			String string2 = target.toString();

			System.out.println(msg);
			System.out.println(string1);
			System.out.println(string2);
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
			case 2:
				// Intentionally not a block
				System.out.println("2");
				break;
			default:
			{
				System.out.println("ok");
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	public void testInitializers()
	{
		Object v = 1;
		v = 0x1;
		v = 0b1;
		v = 1L;
		v = 1l;
		v = 1d;
		v = 1D;
		v = 1f;
		v = 1F;
		v = true;
		v = Boolean.TRUE;
		v = '1';
		new String(new char[] { '1' });
		char[] chars = new char[] { 'o', 'k' };
		char[][] chars2 = { { 'o', 'k' }, { 'n', 'a' } };
	}

	public void testExceptions()
	{
		try
		{
			System.out.println();
			throw new IOException();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			System.out.println();
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
