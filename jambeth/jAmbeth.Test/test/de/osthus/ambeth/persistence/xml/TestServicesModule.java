package de.osthus.ambeth.persistence.xml;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.xml.model.BusinessService;
import de.osthus.ambeth.persistence.xml.model.EmployeeService;
import de.osthus.ambeth.persistence.xml.model.IBusinessService;
import de.osthus.ambeth.persistence.xml.model.IEmployeeService;
import de.osthus.ambeth.persistence.xml.model.IProjectService;
import de.osthus.ambeth.persistence.xml.model.ProjectService;

public class TestServicesModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance(TestServicesModule.class)
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("employeeService", EmployeeService.class).autowireable(IEmployeeService.class);
		beanContextFactory.registerBean("projectService", ProjectService.class).autowireable(IProjectService.class);
		beanContextFactory.registerBean("businessService", BusinessService.class).autowireable(IBusinessService.class);
	}
}
