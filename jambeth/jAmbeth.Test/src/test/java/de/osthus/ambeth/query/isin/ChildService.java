package de.osthus.ambeth.query.isin;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.ParamChecker;

@Service(IChildService.class)
@PersistenceContext
public class ChildService implements IChildService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IQueryBuilderFactory queryBuilderFactory;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(queryBuilderFactory, "queryBuilderFactory");
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	@Override
	public void searchForParentWithEquals(int parentId)
	{
		IQueryBuilder<Child> qb = queryBuilderFactory.create(Child.class);
		qb.selectProperty("Parent.Id");
		qb.selectProperty("Id");
		qb.selectProperty("Version");
		IQuery<Child> query = qb.build(qb.isEqualTo(qb.property("Parent.Id"), qb.value(parentId)));
		query.retrieveAsData();
	}

	@Override
	public void getForParentWithIsIn(int... parentIds)
	{
		IQueryBuilder<Child> qb = queryBuilderFactory.create(Child.class);
		qb.selectProperty("Parent.Id");
		qb.selectProperty("Id");
		qb.selectProperty("Version");
		IQuery<Child> query = qb.build(qb.isIn(qb.property("Parent.Id"), qb.value(parentIds)));
		query.retrieveAsData();
	}
}
