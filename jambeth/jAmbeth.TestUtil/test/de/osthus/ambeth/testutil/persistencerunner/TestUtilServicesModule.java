package de.osthus.ambeth.testutil.persistencerunner;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.model.IProjectService;
import de.osthus.ambeth.testutil.model.ProjectService;

public class TestUtilServicesModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("projectService", ProjectService.class).autowireable(IProjectService.class);
	}
}
