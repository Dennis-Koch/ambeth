package com.koch.ambeth.server.helloworld.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleExtendable;
import com.koch.ambeth.server.helloworld.RandomDataGenerator;
import com.koch.ambeth.server.helloworld.security.TestEntityPrivilegeProvider;
import com.koch.ambeth.server.helloworld.service.HelloWorldService;
import com.koch.ambeth.server.helloworld.service.IHelloWorldService;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;

@BootstrapModule
public class HelloWorldModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("testEntityService", HelloWorldService.class).autowireable(IHelloWorldService.class);

		beanContextFactory.registerBean("randomDataGenerator", RandomDataGenerator.class);
		beanContextFactory.link("randomDataGenerator").to(IJobExtendable.class).with("randomDataGenerator", "* * * * *");

		beanContextFactory.registerBean("testEntityPPE", TestEntityPrivilegeProvider.class);
		beanContextFactory.link("testEntityPPE").to(IEntityPermissionRuleExtendable.class).with(TestEntity.class);
	}
}
