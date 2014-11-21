package de.osthus.ambeth.demo;

import de.osthus.ambeth.demo.ITestInterface;
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

	@Autowired
	protected ITestInterface testService;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		boolean check = true;
		int var = check ? 1 : 0;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
		overloadedMethod((String) null);
	}

	public void overloadedMethod(String stringParam)
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
	}

	public void overloadedMethod(Integer intParam)
	{
		if (intParam == null)
		{
			System.out.println(0);
		}
		else if (intParam < 0)
		{
			System.out.println("neg.");
		}
		else
			System.out.println(intParam);
	}
}
