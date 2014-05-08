package de.osthus.ambeth.service.name;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

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
