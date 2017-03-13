package com.koch.ambeth.persistence.jdbc.ioc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.util.DedicatedConverterUtil;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.copy.IObjectCopierExtendable;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.event.EntityMetaDataRemovedEvent;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.database.DatabaseProvider;
import com.koch.ambeth.persistence.database.IDatabaseFactory;
import com.koch.ambeth.persistence.database.IDatabaseMappedListenerExtendable;
import com.koch.ambeth.persistence.database.IDatabaseMapperExtendable;
import com.koch.ambeth.persistence.database.IDatabaseProviderExtendable;
import com.koch.ambeth.persistence.jdbc.ConnectionHolderInterceptor;
import com.koch.ambeth.persistence.jdbc.ConnectionHolderRegistry;
import com.koch.ambeth.persistence.jdbc.DefaultDatabasePool;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionHolder;
import com.koch.ambeth.persistence.jdbc.IConnectionHolderExtendable;
import com.koch.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import com.koch.ambeth.persistence.jdbc.JDBCDatabaseMetaData;
import com.koch.ambeth.persistence.jdbc.JDBCSqlConnection;
import com.koch.ambeth.persistence.jdbc.JdbcDatabaseFactory;
import com.koch.ambeth.persistence.jdbc.JdbcLink;
import com.koch.ambeth.persistence.jdbc.NoopDatabasePool;
import com.koch.ambeth.persistence.jdbc.TimestampToCalendarConverter;
import com.koch.ambeth.persistence.jdbc.array.ArrayConverter;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.ConnectionFactory;
import com.koch.ambeth.persistence.jdbc.connection.DataSourceConnectionFactory;
import com.koch.ambeth.persistence.jdbc.database.JdbcTransaction;
import com.koch.ambeth.persistence.jdbc.lob.BlobInputSource;
import com.koch.ambeth.persistence.jdbc.lob.BlobInputSourceObjectCopier;
import com.koch.ambeth.persistence.jdbc.lob.ClobInputSource;
import com.koch.ambeth.persistence.jdbc.lob.ClobInputSourceObjectCopier;
import com.koch.ambeth.persistence.jdbc.lob.ClobToEnumConverter;
import com.koch.ambeth.persistence.jdbc.lob.ExtendedCLobConverter;
import com.koch.ambeth.persistence.jdbc.lob.ILobInputSourceController;
import com.koch.ambeth.persistence.jdbc.lob.LobConverter;
import com.koch.ambeth.persistence.jdbc.lob.LobInputSourceController;
import com.koch.ambeth.persistence.jdbc.lob.LobStreamConverter;
import com.koch.ambeth.persistence.sql.ISqlConnection;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;

import net.sf.cglib.proxy.MethodInterceptor;

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

		beanContextFactory.link(IUnmodifiedInputSource.class).to(INoEntityTypeExtendable.class);

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

		IBeanConfiguration clobToEnumConverter = beanContextFactory.registerBean(ClobToEnumConverter.class);
		beanContextFactory.link(clobToEnumConverter, ClobToEnumConverter.HANDLE_ENTITY_META_DATA_ADDED_EVENT).to(IEventListenerExtendable.class)
				.with(EntityMetaDataAddedEvent.class);
		beanContextFactory.link(clobToEnumConverter, ClobToEnumConverter.HANDLE_ENTITY_META_DATA_REMOVED_EVENT).to(IEventListenerExtendable.class)
				.with(EntityMetaDataRemovedEvent.class);

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

		IBeanConfiguration extendedLobConverter = beanContextFactory.registerBean(ExtendedCLobConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean.TYPE);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Character.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Byte.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Short.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer.TYPE);

		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Float.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Long.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Double.class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, boolean[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Byte[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, char[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Character[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, short[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Short[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, int[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, long[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Long[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, float[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Float[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, double[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Double[].class);
		DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, Calendar.class);
		// DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, GregorianCalendar.class);

		IBeanConfiguration lobStreamConverter = beanContextFactory.registerBean(LobStreamConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobStreamConverter, Blob.class, IBinaryInputSource.class);
		DedicatedConverterUtil.biLink(beanContextFactory, lobStreamConverter, Clob.class, ICharacterInputSource.class);

		IBeanConfiguration timestampConverter = beanContextFactory.registerBean(TimestampToCalendarConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, timestampConverter, Long.class, java.util.Calendar.class);
	}
}
