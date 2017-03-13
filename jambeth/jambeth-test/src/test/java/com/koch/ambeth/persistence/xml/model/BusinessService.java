package com.koch.ambeth.persistence.xml.model;

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

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
