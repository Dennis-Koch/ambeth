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
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
		overloadedMethod((String) null);
	}

	public void overloadedMethod(String stringParam)
	{
		System.out.println(stringParam);
	}

	public void overloadedMethod(Integer intParam)
	{
		System.out.println(intParam);
	}
}
