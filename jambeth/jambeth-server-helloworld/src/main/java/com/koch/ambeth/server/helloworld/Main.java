package com.koch.ambeth.server.helloworld;

/*-
 * #%L
 * jambeth-server-helloworld
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

import com.koch.ambeth.informationbus.persistence.setup.AmbethPersistenceSetup;
import com.koch.ambeth.jetty.JettyApplication;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.DatabaseProtocolResolver;
import org.burningwave.core.assembler.StaticComponentContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class Main {
	public static void main(String[] args) throws Throwable {
		new StaticComponentContainer();

		var dbContainer = new PostgreSQLContainer<>("postgres:15-alpine");
		dbContainer.start();

		Properties.getApplication().putString(PersistenceJdbcConfigurationConstants.DatabaseConnection, dbContainer.getJdbcUrl());
		Properties.getApplication().putString(PersistenceJdbcConfigurationConstants.DatabaseName, dbContainer.getDatabaseName());
		Properties.getApplication().putString(PersistenceJdbcConfigurationConstants.DatabaseUser, dbContainer.getUsername());
		Properties.getApplication().putString(PersistenceJdbcConfigurationConstants.DatabasePass, dbContainer.getPassword());
		Properties.getApplication().putString(PersistenceJdbcConfigurationConstants.DatabaseSchemaName, "helloworld");
		DatabaseProtocolResolver.enrichWithDatabaseProtocol(Properties.getApplication());
		var persistenceSetup = new AmbethPersistenceSetup(Main.class).withProperties(Properties.getApplication());
		persistenceSetup.executeSetup(Main.class.getMethod("main", String[].class));

		JettyApplication.run();
	}
}
