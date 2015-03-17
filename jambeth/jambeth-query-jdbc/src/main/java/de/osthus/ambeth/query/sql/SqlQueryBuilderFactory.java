package de.osthus.ambeth.query.sql;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.garbageproxy.IGarbageProxyConstructor;
import de.osthus.ambeth.garbageproxy.IGarbageProxyFactory;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderExtension;
import de.osthus.ambeth.query.IQueryBuilderExtensionExtendable;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

public class SqlQueryBuilderFactory implements IQueryBuilderFactory, IQueryBuilderExtensionExtendable, IInitializingBean
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

	@Autowired
	protected ILightweightTransaction transaction;

	protected final DefaultExtendableContainer<IQueryBuilderExtension> queryBuilderExtensions = new DefaultExtendableContainer<IQueryBuilderExtension>(
			IQueryBuilderExtension.class, "queryBuilderExtension");

	@SuppressWarnings("rawtypes")
	protected IGarbageProxyConstructor<IQueryBuilder> queryBuilderGPC;

	protected final Lock writeLock = new ReentrantLock();

	protected volatile boolean firstQueryBuilder = true;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		queryBuilderGPC = garbageProxyFactory.createGarbageProxyConstructor(IQueryBuilder.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType)
	{
		if (firstQueryBuilder)
		{
			writeLock.lock();
			try
			{
				if (firstQueryBuilder)
				{
					transaction.runInTransaction(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							// intended blank
						}
					});
					firstQueryBuilder = false;
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}
		Class<?> realEntityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		IQueryBuilderExtension[] queryBuilderExtensions = this.queryBuilderExtensions.getExtensions();
		IQueryBuilder<T> sqlQueryBuilder = beanContext.registerBean(SqlQueryBuilder.class)//
				.propertyValue("EntityType", realEntityType)//
				.propertyValue("DisposeContextOnDispose", Boolean.FALSE)//
				.propertyValue("QueryBuilderExtensions", queryBuilderExtensions)//
				.finish();

		return queryBuilderGPC.createInstance(sqlQueryBuilder);
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
