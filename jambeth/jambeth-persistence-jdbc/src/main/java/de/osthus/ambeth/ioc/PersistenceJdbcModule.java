package de.osthus.ambeth.ioc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.DatabaseProvider;
import de.osthus.ambeth.database.IDatabaseFactory;
import de.osthus.ambeth.database.IDatabaseMapperExtendable;
import de.osthus.ambeth.database.IDatabaseProviderExtendable;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.JDBCSqlConnection;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.ConnectionHolderInterceptor;
import de.osthus.ambeth.persistence.jdbc.ConnectionHolderRegistry;
import de.osthus.ambeth.persistence.jdbc.DefaultDatabasePool;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolder;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderExtendable;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import de.osthus.ambeth.persistence.jdbc.JdbcDatabaseFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcLink;
import de.osthus.ambeth.persistence.jdbc.NoopDatabasePool;
import de.osthus.ambeth.persistence.jdbc.TimestampToCalendarConverter;
import de.osthus.ambeth.persistence.jdbc.array.ArrayConverter;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.connection.DataSourceConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.database.JdbcTransaction;
import de.osthus.ambeth.persistence.jdbc.lob.LobConverter;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.sql.ISqlConnection;
import de.osthus.ambeth.util.DedicatedConverterUtil;

@FrameworkModule
public class PersistenceJdbcModule implements IInitializingModule, IPropertyLoadingBean
{
	@Autowired
	protected IProxyFactory proxyFactory;

	@Property(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, defaultValue = "true")
	protected boolean integratedConnectionFactory;

	@Property(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, defaultValue = "true")
	protected boolean integratedConnectionPool;

	@Property(name = PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, mandatory = false)
	protected String additionalConnectionInterfaces;

	@Property(name = PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, mandatory = false)
	protected String additionalConnectionModules;

	@Override
	public void applyProperties(Properties contextProperties)
	{
		String linkType = contextProperties.getString(PersistenceConfigurationConstants.LinkClass);
		if (linkType == null)
		{
			contextProperties.put(PersistenceConfigurationConstants.LinkClass, JdbcLink.class.getName());
		}
		String databaseConnection = contextProperties.getString(PersistenceJdbcConfigurationConstants.DatabaseConnection);
		if (databaseConnection == null)
		{
			contextProperties.put(PersistenceJdbcConfigurationConstants.DatabaseConnection, "${" + PersistenceJdbcConfigurationConstants.DatabaseProtocol
					+ "}:@" + "${" + PersistenceJdbcConfigurationConstants.DatabaseHost + "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabasePort
					+ "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabaseName + "}");
		}
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(ITransaction.class, JdbcTransaction.class).autowireable(ITransactionState.class);

		if (integratedConnectionPool)
		{
			beanContextFactory.registerAutowireableBean(IDatabasePool.class, DefaultDatabasePool.class);
		}
		else
		{
			beanContextFactory.registerAutowireableBean(IDatabasePool.class, NoopDatabasePool.class);
		}

		beanContextFactory.registerBean("databaseProvider", DatabaseProvider.class);
		beanContextFactory.link("databaseProvider").to(IDatabaseProviderExtendable.class).with(Object.class);

		List<Class<?>> connectionModuleTypes = new ArrayList<Class<?>>();
		if (additionalConnectionModules != null)
		{
			String[] typeNames = additionalConnectionModules.split(";");
			for (int a = typeNames.length; a-- > 0;)
			{
				Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(typeNames[a]);
				connectionModuleTypes.add(type);
			}
		}

		beanContextFactory.registerAnonymousBean(JdbcDatabaseFactory.class).propertyRefs("databaseProvider")
				.propertyValue("AdditionalModules", connectionModuleTypes.toArray(new Class<?>[connectionModuleTypes.size()]))
				.autowireable(IDatabaseFactory.class, IDatabaseMapperExtendable.class);

		beanContextFactory.registerAnonymousBean(ConnectionHolderRegistry.class).autowireable(IConnectionHolderRegistry.class,
				IConnectionHolderExtendable.class);

		MethodInterceptor chInterceptor = (MethodInterceptor) beanContextFactory.registerAnonymousBean(ConnectionHolderInterceptor.class)
				.autowireable(IConnectionHolder.class).ignoreProperties("Connection").getInstance();
		beanContextFactory.link(chInterceptor).to(IConnectionHolderExtendable.class).with(Object.class);

		List<Class<?>> connectionInterfaceTypes = new ArrayList<Class<?>>();
		connectionInterfaceTypes.add(Connection.class);
		if (additionalConnectionInterfaces != null)
		{
			String[] typeNames = additionalConnectionInterfaces.split(";");
			for (int a = typeNames.length; a-- > 0;)
			{
				Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(typeNames[a]);
				connectionInterfaceTypes.add(type);
			}
		}
		Class<?>[] cInterfaceTypes = connectionInterfaceTypes.toArray(new Class<?>[connectionInterfaceTypes.size()]);
		Object connectionHolderProxy = proxyFactory.createProxy(cInterfaceTypes, chInterceptor);
		beanContextFactory.registerExternalBean("connectionHolderProxy", connectionHolderProxy).autowireable(cInterfaceTypes);

		if (integratedConnectionFactory)
		{
			beanContextFactory.registerBean("connectionFactory", ConnectionFactory.class).autowireable(IConnectionFactory.class);
		}
		else
		{
			beanContextFactory.registerBean("connectionFactory", DataSourceConnectionFactory.class).autowireable(IConnectionFactory.class);
		}
		beanContextFactory.registerAutowireableBean(ISqlConnection.class, JDBCSqlConnection.class);

		IBeanConfiguration arrayConverter = beanContextFactory.registerAnonymousBean(ArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, boolean[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Boolean[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, char[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Character[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, short[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Short[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, int[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Integer[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, long[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Long[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, float[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Float[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, double[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Double[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, String[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, List.class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Collection.class);
		DedicatedConverterUtil.biLink(beanContextFactory, arrayConverter, Array.class, Set.class);

		IBeanConfiguration lobConverter = beanContextFactory.registerAnonymousBean(LobConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Blob.class, byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Clob.class, char[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Clob.class, String.class);

		IBeanConfiguration timestampConverter = beanContextFactory.registerAnonymousBean(TimestampToCalendarConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, timestampConverter, Long.class, java.util.Calendar.class);
	}
}
