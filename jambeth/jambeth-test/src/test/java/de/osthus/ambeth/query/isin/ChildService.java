package de.osthus.ambeth.query.isin;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;

@Service(IChildService.class)
@PersistenceContext
public class ChildService implements IChildService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

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
