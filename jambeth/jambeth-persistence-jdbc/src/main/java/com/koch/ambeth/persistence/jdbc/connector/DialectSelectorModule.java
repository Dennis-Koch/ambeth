package com.koch.ambeth.persistence.jdbc.connector;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;

import java.util.ServiceLoader;

@FrameworkModule
public class DialectSelectorModule implements IInitializingModule, IPropertyLoadingBean {
    public static void fillProperties(Properties props) {
        String databaseProtocol = props.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
        if (databaseProtocol == null) {
            return;
        }
        IConnector connector = loadConnector(databaseProtocol);
        connector.handleProperties(props, databaseProtocol);
    }

    protected static IConnector loadConnector(String databaseProtocol) {
        var serviceLoader = ServiceLoader.load(IConnector.class);
        return serviceLoader.stream()
                            .map(ServiceLoader.Provider::get)
                            .filter(conn -> conn.supports(databaseProtocol))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No connector found for protocol: '" + databaseProtocol + "'"));
    }
    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol, mandatory = false)
    protected String databaseProtocol;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        if (databaseProtocol == null) {
            // At this point databaseProtocol MUST be initialized
            ParamChecker.assertNotNull(databaseProtocol, "databaseProtocol");
        }
        IConnector connector = loadConnector(databaseProtocol);
        connector.handleProd(beanContextFactory, databaseProtocol);
    }

    @Override
    public void applyProperties(Properties contextProperties) {
        DatabaseProtocolResolver.enrichWithDatabaseProtocol(contextProperties);
    }
}
