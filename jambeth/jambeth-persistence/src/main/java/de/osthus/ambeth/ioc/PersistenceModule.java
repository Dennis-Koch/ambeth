package de.osthus.ambeth.ioc;

import de.osthus.ambeth.database.DatabaseProviderRegistry;
import de.osthus.ambeth.database.DatabaseSessionIdController;
import de.osthus.ambeth.database.IDatabaseProviderExtendable;
import de.osthus.ambeth.database.IDatabaseProviderRegistry;
import de.osthus.ambeth.database.IDatabaseSessionIdController;
import de.osthus.ambeth.database.ITransactionListenerExtendable;
import de.osthus.ambeth.database.ITransactionListenerProvider;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.orm.OrmPatternMatcher;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.EntityLoader;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityLoader;
import de.osthus.ambeth.persistence.ILoadContainerProvider;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.PersistenceHelper;
import de.osthus.ambeth.persistence.callback.IDatabaseLifecycleCallbackExtendable;
import de.osthus.ambeth.persistence.callback.IDatabaseLifecycleCallbackRegistry;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.proxy.PersistencePostProcessor;
import de.osthus.ambeth.proxy.QueryPostProcessor;
import de.osthus.ambeth.proxy.TargetingInterceptor;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ISqlKeywordRegistry;
import de.osthus.ambeth.sql.SqlBuilder;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;

@FrameworkModule
public class PersistenceModule implements IInitializingModule
{
	@Autowired
	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);
		beanContextFactory.registerBean(PersistencePostProcessor.class);

		beanContextFactory.registerBean(QueryPostProcessor.class);

		beanContextFactory.registerBean(EntityLoader.class).autowireable(IEntityLoader.class, ILoadContainerProvider.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, ITransactionListenerProvider.class, ITransactionListenerExtendable.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, IDatabaseLifecycleCallbackRegistry.class, IDatabaseLifecycleCallbackExtendable.class);

		beanContextFactory.registerBean(DatabaseProviderRegistry.class).autowireable(IDatabaseProviderRegistry.class, IDatabaseProviderExtendable.class);

		beanContextFactory.registerBean("databaseSessionIdController", DatabaseSessionIdController.class).autowireable(IDatabaseSessionIdController.class);

		beanContextFactory.registerBean(XmlDatabaseMapper.class).precedence(PrecedenceType.HIGH);

		beanContextFactory.registerBean(OrmPatternMatcher.class).autowireable(IOrmPatternMatcher.class);

		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class, ISqlKeywordRegistry.class);

		TargetingInterceptor databaseInterceptor = (TargetingInterceptor) beanContextFactory.registerBean(TargetingInterceptor.class)
				.propertyRef("TargetProvider", "databaseProvider").getInstance();

		IDatabase databaseTlProxy = proxyFactory.createProxy(IDatabase.class, databaseInterceptor);

		beanContextFactory.registerExternalBean(databaseTlProxy).autowireable(IDatabase.class);

		beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
	}
}
