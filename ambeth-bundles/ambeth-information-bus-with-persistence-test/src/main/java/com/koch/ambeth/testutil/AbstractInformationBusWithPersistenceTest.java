package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.cache.server.ioc.CacheServerModule;
import com.koch.ambeth.event.datachange.ioc.EventDataChangeModule;
import com.koch.ambeth.event.server.ioc.EventServerModule;
import com.koch.ambeth.expr.ioc.ExprModule;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.util.IPropertiesProvider;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.server.ioc.MergeServerModule;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.filter.ioc.FilterPersistenceModule;
import com.koch.ambeth.persistence.ioc.PersistenceModule;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.ioc.PersistenceJdbcModule;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.ioc.QueryModule;
import com.koch.ambeth.query.jdbc.ioc.QueryJdbcModule;
import com.koch.ambeth.security.ioc.PrivilegeModule;
import com.koch.ambeth.security.persistence.ioc.SecurityQueryModule;
import com.koch.ambeth.security.server.ioc.PrivilegeServerModule;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest.PersistencePropertiesProvider;
import com.koch.ambeth.util.IConversionHelper;
import org.junit.Assert;
import org.junit.runner.RunWith;

@TestFrameworkModule({
        AuditModule.class,
        CacheServerModule.class,
        EventServerModule.class,
        EventDataChangeModule.class,
        ExprModule.class,
        IocModule.class,
        MergeServerModule.class,
        PersistenceModule.class,
        PersistenceJdbcModule.class,
        PrivilegeModule.class,
        PrivilegeServerModule.class,
        SecurityServerModule.class,
        SecurityQueryModule.class,
        QueryModule.class,
        QueryJdbcModule.class,
        FilterPersistenceModule.class,
        PreparedStatementParamLoggerModule.class
})
@TestProperties(type = PersistencePropertiesProvider.class)
@RunWith(AmbethInformationBusWithPersistenceRunner.class)
public abstract class AbstractInformationBusWithPersistenceTest extends AbstractInformationBusTest {
    @Autowired
    protected IConnectionFactory connectionFactory;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IEntityFactory entityFactory;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IMeasurement measurement;
    @Autowired
    protected IQueryBuilderFactory queryBuilderFactory;
    @Autowired
    protected ITransaction transaction;

    public void assertProxyEquals(Object expected, Object actual) {
        assertProxyEquals("", expected, actual);
    }

    public void assertProxyEquals(String message, Object expected, Object actual) {
        if (expected == actual) {
            // Nothing to do
            return;
        }
        if (expected == null) {
            if (actual == null) {
                return;
            } else {
                Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
            }
        } else if (actual == null) {
            Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
        }
        if (expected instanceof IEntityEquals) {
            if (expected.equals(actual)) {
                return;
            }
            Assert.fail("expected:<" + expected.toString() + "> but was:<" + actual + ">");
        }
        var expectedMetaData = entityMetaDataProvider.getMetaData(expected.getClass());
        var actualMetaData = entityMetaDataProvider.getMetaData(actual.getClass());
        var expectedType = expectedMetaData.getEntityType();
        var actualType = actualMetaData.getEntityType();
        if (!expectedType.equals(actualType)) {
            Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
        }
        var expectedId = expectedMetaData.getIdMember().getValue(expected, false);
        var actualId = actualMetaData.getIdMember().getValue(actual, false);
        Assert.assertEquals(expectedId, actualId);
    }

    public static class PersistencePropertiesProvider implements IPropertiesProvider {
        @Override
        public void fillProperties(Properties props) {
            // PersistenceJdbcModule
            props.put(ServiceConfigurationConstants.NetworkClientMode, "false");
            props.put(ServiceConfigurationConstants.SlaveMode, "false");
            props.put(ServiceConfigurationConstants.LogShortNames, "true");
            props.put(PersistenceConfigurationConstants.AutoIndexForeignKeys, "true");

            // IocModule
            props.put(IocConfigurationConstants.UseObjectCollector, "false");
        }
    }
}
