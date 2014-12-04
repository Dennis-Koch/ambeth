package de.osthus.ambeth.persistence.xml.model;

import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class BusinessService implements IBusinessService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IEmployeeService employeeService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(employeeService, "employeeService");
	}

	public void setEmployeeService(IEmployeeService employeeService)
	{
		this.employeeService = employeeService;
	}

	@Override
	public List<Employee> retrieve(List<String> names)
	{
		return employeeService.retrieve(names);
	}
}
