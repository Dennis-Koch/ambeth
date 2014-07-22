package de.osthus.ambeth.helloworld.ioc;

import de.osthus.ambeth.helloworld.RandomDataGenerator;
import de.osthus.ambeth.helloworld.security.TestEntityPrivilegeProvider;
import de.osthus.ambeth.helloworld.service.HelloWorldService;
import de.osthus.ambeth.helloworld.service.IHelloWorldService;
import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.job.IJobExtendable;
import de.osthus.ambeth.privilege.IEntityPermissionRuleExtendable;

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
