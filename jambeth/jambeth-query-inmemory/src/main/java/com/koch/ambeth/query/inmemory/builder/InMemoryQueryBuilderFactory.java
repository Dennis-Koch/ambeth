package com.koch.ambeth.query.inmemory.builder;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class InMemoryQueryBuilderFactory implements IQueryBuilderFactory {
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IProxyFactory proxyFactory;

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType) {
		return beanContext.registerBean(InMemoryQueryBuilder.class)
				.propertyValue("EntityType", entityType).finish();
	}
}
