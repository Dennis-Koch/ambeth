package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ClobObject;
import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.ParamChecker;

@MergeContext
public class ClobObjectService implements IClobObjectService, IInitializingBean, IStartingBean
{
	protected static final String param = "ids";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<ClobObject> getClobObjectsQuery;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(queryBuilderFactory, "queryBuilderFactory");
	}

	@Override
	public void afterStarted() throws Throwable
	{
		IQueryBuilder<ClobObject> qb = queryBuilderFactory.create(ClobObject.class);
		getClobObjectsQuery = qb.build(qb.isIn(qb.property("Id"), qb.valueName(param)));
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	@Override
	public List<ClobObject> getClobObjects(Integer... id)
	{
		return getClobObjectsQuery.param(param, id).retrieve();
	}

	@Override
	public void updateClobObject(ClobObject clobObject)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteClobObject(ClobObject clobObject)
	{
		throw new UnsupportedOperationException();
	}
}
