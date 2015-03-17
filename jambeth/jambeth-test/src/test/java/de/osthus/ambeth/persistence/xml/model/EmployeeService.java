package de.osthus.ambeth.persistence.xml.model;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.OrderByType;

@Service(IEmployeeService.class)
@PersistenceContext
public class EmployeeService implements IInitializingBean, IEmployeeService, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Employee> queryEmployeeByName;

	protected IQuery<Employee> queryEmployeeAll;

	protected IQuery<Employee> queryEmployeeByNameOrderedAsc;

	protected IQuery<Employee> queryEmployeeByNameOrderedDesc;

	@Override
	public void afterPropertiesSet() throws Throwable
	{

	}

	@Override
	public void afterStarted() throws Throwable
	{
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			queryEmployeeByName = qb.build(qb.isEqualTo(qb.property(Employee.Name), qb.valueName(Employee.Name)));
		}
		{
			queryEmployeeAll = queryBuilderFactory.create(Employee.class).build();
		}
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			IOperand nameOp = qb.property(Employee.Name);
			qb.orderBy(nameOp, OrderByType.ASC);
			queryEmployeeByNameOrderedAsc = qb.build();
		}
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			IOperand nameOp = qb.property(Employee.Name);
			qb.orderBy(nameOp, OrderByType.DESC);
			queryEmployeeByNameOrderedDesc = qb.build();
		}
	}

	@Override
	public List<Employee> getAll()
	{
		return queryEmployeeAll.retrieve();
	}

	@Override
	public Employee getByName(String name)
	{
		return queryEmployeeByName.param(Employee.Name, name).retrieveSingle();
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
		if (reverse)
		{
			return queryEmployeeByNameOrderedDesc.retrieve();
		}
		return queryEmployeeByNameOrderedAsc.retrieve();
	}

	@Override
	public Boat saveBoat(Boat boat)
	{
		throw new UnsupportedOperationException();
	}
}
