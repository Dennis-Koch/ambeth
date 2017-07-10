package com.koch.ambeth.service.name;

import com.koch.ambeth.service.proxy.Service;

@Service(value = ITestService.class, name = "TestService 2")
public class TestService2 implements ITestService {
	@Override
	public void testCallOnNamedService() {
	}
}
