package com.koch.ambeth.persistence.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.orm.blueprint.IOrmDatabaseMapper;
import com.koch.ambeth.persistence.EntityLoader;
import com.koch.ambeth.persistence.IEntityLoader;
import com.koch.ambeth.persistence.ILoadContainerProvider;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.PersistenceHelper;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ITransactionListenerExtendable;
import com.koch.ambeth.persistence.api.database.ITransactionListenerProvider;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.callback.IDatabaseLifecycleCallbackExtendable;
import com.koch.ambeth.persistence.callback.IDatabaseLifecycleCallbackRegistry;
import com.koch.ambeth.persistence.database.DatabaseProviderRegistry;
import com.koch.ambeth.persistence.database.DatabaseSessionIdController;
import com.koch.ambeth.persistence.database.IDatabaseProviderExtendable;
import com.koch.ambeth.persistence.database.IDatabaseProviderRegistry;
import com.koch.ambeth.persistence.database.IDatabaseSessionIdController;
import com.koch.ambeth.persistence.orm.IOrmPatternMatcher;
import com.koch.ambeth.persistence.orm.OrmPatternMatcher;
import com.koch.ambeth.persistence.orm.XmlDatabaseMapper;
import com.koch.ambeth.persistence.proxy.PersistencePostProcessor;
import com.koch.ambeth.persistence.proxy.QueryPostProcessor;
import com.koch.ambeth.persistence.sql.ISqlKeywordRegistry;
import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.persistence.util.IPersistenceExceptionUtil;
import com.koch.ambeth.persistence.util.PersistenceExceptionUtil;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.proxy.TargetingInterceptor;

@FrameworkModule
public class PersistenceModule implements IInitializingModule {
	@Autowired
	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);
		beanContextFactory.registerBean(PersistencePostProcessor.class);

		beanContextFactory.registerBean(QueryPostProcessor.class);

		beanContextFactory.registerBean(EntityLoader.class).autowireable(IEntityLoader.class,
				ILoadContainerProvider.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, ITransactionListenerProvider.class,
				ITransactionListenerExtendable.class, ITransactionListenerProvider.class.getClassLoader());

		ExtendableBean.registerExtendableBean(beanContextFactory,
				IDatabaseLifecycleCallbackRegistry.class, IDatabaseLifecycleCallbackExtendable.class,
				IDatabaseLifecycleCallbackExtendable.class.getClassLoader());

		beanContextFactory.registerBean(DatabaseProviderRegistry.class)
				.autowireable(IDatabaseProviderRegistry.class, IDatabaseProviderExtendable.class);

		beanContextFactory
				.registerBean("databaseSessionIdController", DatabaseSessionIdController.class)
				.autowireable(IDatabaseSessionIdController.class);

		beanContextFactory.registerBean(XmlDatabaseMapper.class).precedence(PrecedenceType.HIGH)
				.autowireable(IOrmDatabaseMapper.class);

		beanContextFactory.registerBean(OrmPatternMatcher.class).autowireable(IOrmPatternMatcher.class);

		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class,
				ISqlKeywordRegistry.class);

		TargetingInterceptor databaseInterceptor =
				(TargetingInterceptor) beanContextFactory.registerBean(TargetingInterceptor.class)
						.propertyRef("TargetProvider", "databaseProvider").getInstance();

		IDatabase databaseTlProxy =
				proxyFactory.createProxy(getClass().getClassLoader(), IDatabase.class, databaseInterceptor);

		beanContextFactory.registerExternalBean(databaseTlProxy).autowireable(IDatabase.class);

		beanContextFactory.registerBean(PersistenceExceptionUtil.class)
				.autowireable(IPersistenceExceptionUtil.class);
	}
}
