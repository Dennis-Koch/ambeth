package com.koch.ambeth.service.name;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

@Service(value = ITestService.class, name = "TestService 2")
public class TestService2 implements ITestService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void testCallOnNamedService()
	{
	}
}
