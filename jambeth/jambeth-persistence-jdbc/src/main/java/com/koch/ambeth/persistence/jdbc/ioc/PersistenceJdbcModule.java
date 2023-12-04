package com.koch.ambeth.persistence.jdbc.ioc;

/*-
 * #%L
 * jambeth-persistence-jdbc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.copy.IObjectCopierExtendable;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.event.EntityMetaDataRemovedEvent;
import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.persistence.NoopDatabasePool;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
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
import com.koch.ambeth.persistence.jdbc.IConnectionHolderExtendable;
import com.koch.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import com.koch.ambeth.persistence.jdbc.JDBCDatabaseMetaData;
import com.koch.ambeth.persistence.jdbc.JDBCSqlConnection;
import com.koch.ambeth.persistence.jdbc.JdbcDatabaseFactory;
import com.koch.ambeth.persistence.jdbc.JdbcLink;
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
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.proxy.MethodInterceptor;
import com.koch.ambeth.util.transaction.ILightweightTransaction;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;
import io.toolisticon.spiap.api.SpiService;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.koch.ambeth.ioc.util.DedicatedConverterUtil.biLink;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class PersistenceJdbcModule implements IFrameworkModule, IPropertyLoadingBean {
    @Autowired
    protected IClassCache classCache;

    @Autowired
    protected IProxyFactory proxyFactory;

    @Property(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, defaultValue = "true")
    protected boolean integratedConnectionFactory;

    @Property(name = PersistenceJdbcConfigurationConstants.IntegratedConnectionPool, defaultValue = "true")
    protected boolean integratedConnectionPool;

    @Property(name = PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, mandatory = false)
    protected String additionalConnectionModules;

    @Override
    public void applyProperties(Properties contextProperties) {
        String linkType = contextProperties.getString(PersistenceConfigurationConstants.LinkClass);
        if (linkType == null) {
            contextProperties.put(PersistenceConfigurationConstants.LinkClass, JdbcLink.class.getName());
        }
    }

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(JdbcTransaction.class).autowireable(ILightweightTransaction.class, ITransaction.class, ITransactionState.class);

        if (integratedConnectionPool) {
            beanContextFactory.registerAutowireableBean(IDatabasePool.class, DefaultDatabasePool.class);
        } else {
            beanContextFactory.registerAutowireableBean(IDatabasePool.class, NoopDatabasePool.class);
        }

        var databaseProvider = beanContextFactory.registerBean(IDatabaseProvider.DEFAULT_DATABASE_PROVIDER_NAME, DatabaseProvider.class);
        beanContextFactory.link(databaseProvider).to(IDatabaseProviderExtendable.class).with(Object.class);

        beanContextFactory.registerBean(JDBCDatabaseMetaData.class).propertyValue("DefaultVersionFieldName", "Version")//
                          .propertyValue("DefaultCreatedByFieldName", "Created_By")//
                          .propertyValue("DefaultCreatedOnFieldName", "Created_On")//
                          .propertyValue("DefaultUpdatedByFieldName", "Updated_By")//
                          .propertyValue("DefaultUpdatedOnFieldName", "Updated_On")//
                          .autowireable(IDatabaseMetaData.class, IDatabaseMappedListenerExtendable.class);

        var connectionModuleTypes = new ArrayList<Class<?>>();
        if (additionalConnectionModules != null) {
            var typeNames = additionalConnectionModules.split(";");
            for (int a = typeNames.length; a-- > 0; ) {
                var type = classCache.loadClass(typeNames[a]);
                connectionModuleTypes.add(type);
            }
        }

        beanContextFactory.link(IUnmodifiedInputSource.class).to(INoEntityTypeExtendable.class);

        beanContextFactory.registerBean(JdbcDatabaseFactory.class)
                          .propertyRefs(IDatabaseProvider.DEFAULT_DATABASE_PROVIDER_NAME)
                          .propertyValue(JdbcDatabaseFactory.ADDITIONAL_MODULES_PROP, connectionModuleTypes.toArray(new Class<?>[connectionModuleTypes.size()]))
                          .autowireable(IDatabaseFactory.class, IDatabaseMapperExtendable.class);

        beanContextFactory.registerBean(ConnectionHolderRegistry.class).autowireable(IConnectionHolderRegistry.class, IConnectionHolderExtendable.class);

        var chInterceptor = (MethodInterceptor) beanContextFactory.registerBean(ConnectionHolderInterceptor.class)
                                                                  .autowireable(IConnectionHolder.class)
                                                                  .ignoreProperties(ConnectionHolderInterceptor.P_CONNECTION)
                                                                  .getInstance();
        beanContextFactory.link(chInterceptor).to(IConnectionHolderExtendable.class).with(Object.class);

        var connectionHolderProxy = proxyFactory.createProxy(Connection.class, chInterceptor);
        beanContextFactory.registerExternalBean("connectionHolderProxy", connectionHolderProxy).autowireable(Connection.class);

        if (integratedConnectionFactory) {
            beanContextFactory.registerBean(ConnectionFactory.class).autowireable(IConnectionFactory.class);
        } else {
            beanContextFactory.registerBean(DataSourceConnectionFactory.class).autowireable(IConnectionFactory.class);
        }
        beanContextFactory.registerAutowireableBean(ISqlConnection.class, JDBCSqlConnection.class);

        beanContextFactory.registerBean(LobInputSourceController.class).autowireable(ILobInputSourceController.class);

        var blobInputSourceObjectCopier = beanContextFactory.registerBean(BlobInputSourceObjectCopier.class);
        beanContextFactory.link(blobInputSourceObjectCopier).to(IObjectCopierExtendable.class).with(BlobInputSource.class);

        var clobInputSourceObjectCopier = beanContextFactory.registerBean(ClobInputSourceObjectCopier.class);
        beanContextFactory.link(clobInputSourceObjectCopier).to(IObjectCopierExtendable.class).with(ClobInputSource.class);

        var clobToEnumConverter = beanContextFactory.registerBean(ClobToEnumConverter.class);
        beanContextFactory.link(clobToEnumConverter, ClobToEnumConverter.HANDLE_ENTITY_META_DATA_ADDED_EVENT).to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);
        beanContextFactory.link(clobToEnumConverter, ClobToEnumConverter.HANDLE_ENTITY_META_DATA_REMOVED_EVENT).to(IEventListenerExtendable.class).with(EntityMetaDataRemovedEvent.class);

        var arrayConverter = beanContextFactory.registerBean(ArrayConverter.class);
        biLink(beanContextFactory, arrayConverter, Array.class, boolean[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Boolean[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, byte[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Byte[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, char[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Character[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, short[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Short[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, int[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Integer[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, long[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Long[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, float[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Float[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, double[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, Double[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, String[].class);
        biLink(beanContextFactory, arrayConverter, Array.class, List.class);
        biLink(beanContextFactory, arrayConverter, Array.class, Collection.class);
        biLink(beanContextFactory, arrayConverter, Array.class, Set.class);

        var lobConverter = beanContextFactory.registerBean(LobConverter.class);
        biLink(beanContextFactory, lobConverter, Blob.class, byte[].class);
        biLink(beanContextFactory, lobConverter, Clob.class, char[].class);
        biLink(beanContextFactory, lobConverter, Clob.class, String.class);

        var extendedLobConverter = beanContextFactory.registerBean(ExtendedCLobConverter.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean.TYPE);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Character.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Byte.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Short.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer.TYPE);

        biLink(beanContextFactory, extendedLobConverter, Clob.class, Float.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Long.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Double.class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, boolean[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Boolean[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, byte[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Byte[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, char[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Character[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, short[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Short[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, int[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Integer[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, long[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Long[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, float[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Float[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, double[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Double[].class);
        biLink(beanContextFactory, extendedLobConverter, Clob.class, Calendar.class);
        // DedicatedConverterUtil.biLink(beanContextFactory, extendedLobConverter, Clob.class, GregorianCalendar.class);

        var lobStreamConverter = beanContextFactory.registerBean(LobStreamConverter.class);
        biLink(beanContextFactory, lobStreamConverter, Blob.class, IBinaryInputSource.class);
        biLink(beanContextFactory, lobStreamConverter, Clob.class, ICharacterInputSource.class);

        var timestampConverter = beanContextFactory.registerBean(TimestampToCalendarConverter.class);
        biLink(beanContextFactory, timestampConverter, Long.class, java.util.Calendar.class);
    }
}
