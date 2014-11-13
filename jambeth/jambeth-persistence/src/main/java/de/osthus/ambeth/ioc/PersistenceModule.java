package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.DatabaseProviderRegistry;
import de.osthus.ambeth.database.DatabaseSessionIdController;
import de.osthus.ambeth.database.IDatabaseProviderExtendable;
import de.osthus.ambeth.database.IDatabaseProviderRegistry;
import de.osthus.ambeth.database.IDatabaseSessionIdController;
import de.osthus.ambeth.database.ITransactionListenerExtendable;
import de.osthus.ambeth.database.ITransactionListenerProvider;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.EntityLoader;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityLoader;
import de.osthus.ambeth.persistence.ILoadContainerProvider;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.PersistenceHelper;
import de.osthus.ambeth.persistence.callback.IDatabaseLifecycleCallbackExtendable;
import de.osthus.ambeth.persistence.callback.IDatabaseLifecycleCallbackRegistry;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.parallel.EntityLoaderParallelInvoker;
import de.osthus.ambeth.persistence.parallel.IEntityLoaderParallelInvoker;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.proxy.PersistencePostProcessor;
import de.osthus.ambeth.proxy.QueryPostProcessor;
import de.osthus.ambeth.proxy.TargetingInterceptor;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ISqlKeywordRegistry;
import de.osthus.ambeth.sql.SqlBuilder;
import de.osthus.ambeth.threading.FastThreadPool;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

@FrameworkModule
public class PersistenceModule implements IInitializingModule
{
	public static final String DEFAULT_PARALLEL_READ_EXECUTOR_NAME = "entityLoaderExecutorService";

	@Property(name = PersistenceConfigurationConstants.ParallelReadActive, defaultValue = "true")
	protected boolean parallelReadActive;

	@Property(name = PersistenceConfigurationConstants.ParallelReadExecutorName, defaultValue = DEFAULT_PARALLEL_READ_EXECUTOR_NAME)
	protected String parallelReadExecutorName;

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

		IBeanConfiguration entityLoaderParallelInvokerBC = beanContextFactory.registerBean(EntityLoaderParallelInvoker.class)
				.propertyRefs("databaseProvider").autowireable(IEntityLoaderParallelInvoker.class);
		if (parallelReadActive)
		{
			entityLoaderParallelInvokerBC.propertyRefs(parallelReadExecutorName);

			if (DEFAULT_PARALLEL_READ_EXECUTOR_NAME.equals(parallelReadExecutorName))
			{
				FastThreadPool entityLoaderExecutorService = new FastThreadPool(0, Integer.MAX_VALUE, 60000);
				entityLoaderExecutorService.setVariableThreads(false);
				entityLoaderExecutorService.setName("ELP");

				beanContextFactory.registerExternalBean(parallelReadExecutorName, entityLoaderExecutorService);
			}
		}

		ExtendableBean.registerExtendableBean(beanContextFactory, IDatabaseLifecycleCallbackRegistry.class, IDatabaseLifecycleCallbackExtendable.class);

		beanContextFactory.registerBean(DatabaseProviderRegistry.class).autowireable(IDatabaseProviderRegistry.class,
				IDatabaseProviderExtendable.class);

		beanContextFactory.registerBean("databaseSessionIdController", DatabaseSessionIdController.class).autowireable(IDatabaseSessionIdController.class);

		beanContextFactory.registerBean(XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		beanContextFactory.registerBean(XmlDatabaseMapper.class).precedence(PrecedenceType.HIGH);

		IBeanConfiguration ormXmlReaderLegathy = beanContextFactory.registerBean(OrmXmlReaderLegathy.class);

		beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class).propertyRef(ExtendableBean.P_DEFAULT_BEAN, ormXmlReaderLegathy)
				.autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);

		IBeanConfiguration ormXmlReader20BC = beanContextFactory.registerBean(OrmXmlReader20.class);
		beanContextFactory.link(ormXmlReader20BC).to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);

		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class, ISqlKeywordRegistry.class);

		TargetingInterceptor databaseInterceptor = (TargetingInterceptor) beanContextFactory.registerBean(TargetingInterceptor.class)
				.propertyRef("TargetProvider", "databaseProvider").getInstance();

		IDatabase databaseTlProxy = proxyFactory.createProxy(IDatabase.class, databaseInterceptor);

		beanContextFactory.registerExternalBean(databaseTlProxy).autowireable(IDatabase.class);

		beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
	}
}
