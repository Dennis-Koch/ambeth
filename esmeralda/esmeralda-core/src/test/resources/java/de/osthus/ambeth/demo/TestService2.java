package de.osthus.ambeth.demo;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class TestService2 implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(value = "test", optional = true)
	protected ITestInterface testService;

	@Autowired("test2")
	protected ITestInterface testService2;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		boolean check = true;
		int var = check ? 1 : 0;
		ParamChecker.assertTrue(var == 1, "var");

		TestService1.staticTestMethod();
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
		overloadedMethod((String) null);
	}

	public boolean overloadedMethod(String stringParam)
	{
		boolean print = true;
		while (print)
		{
			System.out.println(stringParam);
			print = false;
		}
		print = true;
		do
		{
			System.out.println(stringParam);
			print = false;
		}
		while (print);

		return print;
	}

	public boolean overloadedMethod(Integer intParam)
	{
		boolean ok = false;
		if (intParam == null)
		{
			System.out.println(0);
		}
		else if (intParam < 0)
		{
			System.out.println("neg.");
		}
		else
		{
			System.out.println(intParam);
			ok = true;
		}

		return ok;
	}

	public void anonymousClassInstance()
	{
		final int hallo = 5;
		new AbstractObject()
		{
			@Override
			public void method2()
			{
				System.out.println("hello " + hallo);
			}
		}.method2();
	}

	public void callToLog()
	{
		log.debug("Hello");
		log.error(new RuntimeException());
	}
}
