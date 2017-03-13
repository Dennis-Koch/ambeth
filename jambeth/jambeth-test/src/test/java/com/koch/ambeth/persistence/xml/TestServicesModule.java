package com.koch.ambeth.persistence.xml;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.xml.model.BusinessService;
import com.koch.ambeth.persistence.xml.model.EmployeeService;
import com.koch.ambeth.persistence.xml.model.IBusinessService;
import com.koch.ambeth.persistence.xml.model.IEmployeeService;
import com.koch.ambeth.persistence.xml.model.IProjectService;
import com.koch.ambeth.persistence.xml.model.ProjectService;

public class TestServicesModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("employeeService", EmployeeService.class).autowireable(IEmployeeService.class);
		beanContextFactory.registerBean("projectService", ProjectService.class).autowireable(IProjectService.class);
		beanContextFactory.registerBean("businessService", BusinessService.class).autowireable(IBusinessService.class);
	}
}
