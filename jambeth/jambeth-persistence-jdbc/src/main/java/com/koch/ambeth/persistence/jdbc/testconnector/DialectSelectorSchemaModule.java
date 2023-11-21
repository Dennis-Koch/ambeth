package com.koch.ambeth.persistence.jdbc.testconnector;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.IConnector;

import java.util.ServiceLoader;

public class DialectSelectorSchemaModule implements IInitializingModule {
	public static ITestConnector loadTestConnector(String databaseProtocol) {
        var serviceLoader = ServiceLoader.load(ITestConnector.class);
        var connector = serviceLoader.stream().map(ServiceLoader.Provider::get).filter(conn -> conn.supports(databaseProtocol)).findFirst().orElse(null);


		// String connectorName = databaseProtocol.toUpperCase().replace(':', '_');
		var classLoader = Thread.currentThread().getContextClassLoader();

		var connectorName = databaseProtocol.toLowerCase().replace(':', '.') + '.'
				+ databaseProtocol.toUpperCase().replace(':', '_');
		var fqConnectorName = DialectSelectorSchemaModule.class.getPackage().getName() + "."
				+ connectorName;
		try {
			var connectorType = classLoader.loadClass(fqConnectorName);
			return (ITestConnector) connectorType.newInstance();
		}
		catch (Throwable e) {
			try {
				var connectorType = DialectSelectorSchemaModule.class.getClassLoader()
						.loadClass(fqConnectorName);
				return (ITestConnector) connectorType.newInstance();
			}
			catch (Throwable e2) {
				throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'", e);
			}
		}
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
	protected String databaseProtocol;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		ITestConnector connector = loadTestConnector(databaseProtocol);
		connector.handleTestSetup(beanContextFactory, databaseProtocol);
	}
}
