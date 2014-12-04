package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
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

	protected final DefaultExtendableContainer<IQueryBuilderExtension> queryBuilderExtensions = new DefaultExtendableContainer<IQueryBuilderExtension>(
			IQueryBuilderExtension.class, "queryBuilderExtension");

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(final Class<T> entityType)
	{
		IQueryBuilderExtension[] queryBuilderExtensions = this.queryBuilderExtensions.getExtensions();
		return beanContext.registerBean(SqlQueryBuilder.class)//
				.propertyValue("EntityType", entityType)//
				.propertyValue("DisposeContextOnDispose", Boolean.FALSE)//
				.propertyValue("QueryBuilderExtensions", queryBuilderExtensions)//
				.finish();
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
