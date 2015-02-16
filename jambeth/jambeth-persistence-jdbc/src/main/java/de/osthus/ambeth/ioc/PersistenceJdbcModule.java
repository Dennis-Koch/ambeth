package de.osthus.ambeth.ioc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.copy.IObjectCopierExtendable;
import de.osthus.ambeth.database.DatabaseProvider;
import de.osthus.ambeth.database.IDatabaseFactory;
import de.osthus.ambeth.database.IDatabaseMappedListenerExtendable;
import de.osthus.ambeth.database.IDatabaseMapperExtendable;
import de.osthus.ambeth.database.IDatabaseProviderExtendable;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IDatabasePool;
import de.osthus.ambeth.persistence.JDBCSqlConnection;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.ConnectionHolderInterceptor;
import de.osthus.ambeth.persistence.jdbc.ConnectionHolderRegistry;
import de.osthus.ambeth.persistence.jdbc.DefaultDatabasePool;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolder;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderExtendable;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import de.osthus.ambeth.persistence.jdbc.JDBCDatabaseMetaData;
import de.osthus.ambeth.persistence.jdbc.JdbcDatabaseFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcLink;
import de.osthus.ambeth.persistence.jdbc.NoopDatabasePool;
import de.osthus.ambeth.persistence.jdbc.TimestampToCalendarConverter;
import de.osthus.ambeth.persistence.jdbc.array.ArrayConverter;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.connection.DataSourceConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.database.JdbcTransaction;
import de.osthus.ambeth.persistence.jdbc.lob.BlobInputSource;
import de.osthus.ambeth.persistence.jdbc.lob.BlobInputSourceObjectCopier;
import de.osthus.ambeth.persistence.jdbc.lob.ClobInputSource;
import de.osthus.ambeth.persistence.jdbc.lob.ClobInputSourceObjectCopier;
import de.osthus.ambeth.persistence.jdbc.lob.ILobInputSourceController;
import de.osthus.ambeth.persistence.jdbc.lob.LobConverter;
import de.osthus.ambeth.persistence.jdbc.lob.LobInputSourceController;
import de.osthus.ambeth.persistence.jdbc.lob.LobStreamConverter;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.sql.ISqlConnection;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;
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
		beanContextFactory.registerBean(JdbcTransaction.class).autowireable(ILightweightTransaction.class, ITransaction.class, ITransactionState.class);

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

		beanContextFactory.registerBean(JDBCDatabaseMetaData.class).propertyValue("DefaultVersionFieldName", "Version")//
				.propertyValue("DefaultCreatedByFieldName", "Created_By")//
				.propertyValue("DefaultCreatedOnFieldName", "Created_On")//
				.propertyValue("DefaultUpdatedByFieldName", "Updated_By")//
				.propertyValue("DefaultUpdatedOnFieldName", "Updated_On")//
				.autowireable(IDatabaseMetaData.class, IDatabaseMappedListenerExtendable.class);

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

		beanContextFactory.registerBean(JdbcDatabaseFactory.class).propertyRefs("databaseProvider")
				.propertyValue("AdditionalModules", connectionModuleTypes.toArray(new Class<?>[connectionModuleTypes.size()]))
				.autowireable(IDatabaseFactory.class, IDatabaseMapperExtendable.class);

		beanContextFactory.registerBean(ConnectionHolderRegistry.class).autowireable(IConnectionHolderRegistry.class, IConnectionHolderExtendable.class);

		MethodInterceptor chInterceptor = (MethodInterceptor) beanContextFactory.registerBean(ConnectionHolderInterceptor.class)
				.autowireable(IConnectionHolder.class).ignoreProperties("Connection").getInstance();
		beanContextFactory.link(chInterceptor).to(IConnectionHolderExtendable.class).with(Object.class);

		Object connectionHolderProxy = proxyFactory.createProxy(Connection.class, chInterceptor);
		beanContextFactory.registerExternalBean("connectionHolderProxy", connectionHolderProxy).autowireable(Connection.class);

		if (integratedConnectionFactory)
		{
			beanContextFactory.registerBean("connectionFactory", ConnectionFactory.class).autowireable(IConnectionFactory.class);
		}
		else
		{
			beanContextFactory.registerBean("connectionFactory", DataSourceConnectionFactory.class).autowireable(IConnectionFactory.class);
		}
		beanContextFactory.registerAutowireableBean(ISqlConnection.class, JDBCSqlConnection.class);

		beanContextFactory.registerBean(LobInputSourceController.class).autowireable(ILobInputSourceController.class);

		IBeanConfiguration blobInputSourceObjectCopier = beanContextFactory.registerBean(BlobInputSourceObjectCopier.class);
		beanContextFactory.link(blobInputSourceObjectCopier).to(IObjectCopierExtendable.class).with(BlobInputSource.class);

		IBeanConfiguration clobInputSourceObjectCopier = beanContextFactory.registerBean(ClobInputSourceObjectCopier.class);
		beanContextFactory.link(clobInputSourceObjectCopier).to(IObjectCopierExtendable.class).with(ClobInputSource.class);

		IBeanConfiguration arrayConverter = beanContextFactory.registerBean(ArrayConverter.class);
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

		IBeanConfiguration lobConverter = beanContextFactory.registerBean(LobConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Blob.class, byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Clob.class, char[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobConverter, Clob.class, String.class);

		IBeanConfiguration lobStreamConverter = beanContextFactory.registerBean(LobStreamConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobStreamConverter, Blob.class, IBinaryInputSource.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobStreamConverter, Clob.class, ICharacterInputSource.class);

		IBeanConfiguration timestampConverter = beanContextFactory.registerBean(TimestampToCalendarConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, timestampConverter, Long.class, java.util.Calendar.class);
	}
}
