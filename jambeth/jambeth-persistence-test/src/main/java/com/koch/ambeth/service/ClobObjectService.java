package com.koch.ambeth.service;

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.model.ClobObject;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.ParamChecker;

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
