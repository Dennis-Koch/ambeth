package com.koch.ambeth.query.isin;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.proxy.Service;

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
		query.retrieveAsData().dispose();
	}

	@Override
	public void getForParentWithIsIn(int... parentIds)
	{
		IQueryBuilder<Child> qb = queryBuilderFactory.create(Child.class);
		qb.selectProperty("Parent.Id");
		qb.selectProperty("Id");
		qb.selectProperty("Version");
		IQuery<Child> query = qb.build(qb.isIn(qb.property("Parent.Id"), qb.value(parentIds)));
		query.retrieveAsData().dispose();
	}
}
