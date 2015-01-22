package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.garbageproxy.IGarbageProxyFactory;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderExtension;
import de.osthus.ambeth.query.IQueryBuilderExtensionExtendable;
import de.osthus.ambeth.query.IQueryBuilderFactory;

public class SqlQueryBuilderFactory implements IQueryBuilderFactory, IQueryBuilderExtensionExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	protected final DefaultExtendableContainer<IQueryBuilderExtension> queryBuilderExtensions = new DefaultExtendableContainer<IQueryBuilderExtension>(
			IQueryBuilderExtension.class, "queryBuilderExtension");

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType)
	{
		Class<?> realEntityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		IQueryBuilderExtension[] queryBuilderExtensions = this.queryBuilderExtensions.getExtensions();
		IQueryBuilder<T> sqlQueryBuilder = beanContext.registerBean(SqlQueryBuilder.class)//
				.propertyValue("EntityType", realEntityType)//
				.propertyValue("DisposeContextOnDispose", Boolean.FALSE)//
				.propertyValue("QueryBuilderExtensions", queryBuilderExtensions)//
				.finish();

		return garbageProxyFactory.createGarbageProxy(sqlQueryBuilder, IQueryBuilder.class);
	}

	@Override
	public void registerQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension)
	{
		queryBuilderExtensions.register(queryBuilderExtension);
	}

	@Override
	public void unregisterQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension)
	{
		queryBuilderExtensions.unregister(queryBuilderExtension);
	}
}
