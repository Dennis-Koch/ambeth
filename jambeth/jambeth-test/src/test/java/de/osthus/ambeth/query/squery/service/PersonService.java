package de.osthus.ambeth.query.squery.service;

import java.util.List;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.squery.model.Person;

@Service(IPersonService.class)
@MergeContext
public abstract class PersonService implements IPersonService
{
	public static final String CONCRETE_METHOD_RETURN_VALUE = "this method will not be intercepted";

	@Autowired
	protected IQueryBuilderFactory qbf;

	@Override
	public String findByConcreteMethod(String anyValue)
	{
		return CONCRETE_METHOD_RETURN_VALUE;
	}

	/**
	 * this is not abstract and not be declared in the implements interface
	 * 
	 * @return empty list
	 */
	public List<Person> findByNoSquery(Integer minAge)
	{
		IQueryBuilder<Person> qb = qbf.create(Person.class);
		IOperator where = qb.isGreaterThanOrEqualTo(qb.property(Person.AGE), qb.value(minAge));
		return qb.build(where).retrieve();
	}

	/**
	 * this is abstract and not be declared in the implements interface, this method have Squery feature
	 * 
	 * @return not supply the return value
	 */
	public abstract List<Person> findByAgeLe(Integer maxAge);
}
