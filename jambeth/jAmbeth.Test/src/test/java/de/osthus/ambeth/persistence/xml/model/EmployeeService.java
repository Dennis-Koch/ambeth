package de.osthus.ambeth.persistence.xml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IEmployeeService.class)
@PersistenceContext
public class EmployeeService implements IInitializingBean, IEmployeeService
{
	@SuppressWarnings("unused")
	@LogInstance(EmployeeService.class)
	private ILogger log;

	protected IDatabase database;

	protected IServiceUtil serviceUtil;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
		ParamChecker.assertNotNull(database, "database");
	}

	public void setServiceUtil(IServiceUtil serviceUtil)
	{
		this.serviceUtil = serviceUtil;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	@Override
	public List<Employee> getAll()
	{
		List<Employee> employees = new ArrayList<Employee>();
		serviceUtil.loadObjectsIntoCollection(employees, Employee.class, database.getTableByType(Employee.class).selectAll());
		return employees;
	}

	@Override
	public Employee getByName(String name)
	{
		IField nameField = database.getTableByType(Employee.class).getFieldByMemberName("Name");
		return serviceUtil.loadObject(Employee.class, nameField.findSingle(name));
	}

	@Override
	public List<Employee> retrieve(List<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(Employee employee)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(List<Employee> employees)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Employee employee)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(List<Employee> employees)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Set<Employee> employees)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Employee[] employees)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Employee> retrieveOrderedByName(boolean reverse)
	{
		List<Employee> result = new ArrayList<Employee>();
		ITable table = database.getTableByType(Employee.class);
		serviceUtil.loadObjectsIntoCollection(result, Employee.class, table.selectVersionWhere("1=1 ORDER BY \"NAME\"" + (reverse ? " DESC" : "")));
		return result;
	}

	@Override
	public Boat saveBoat(Boat boat)
	{
		throw new UnsupportedOperationException();
	}
}
