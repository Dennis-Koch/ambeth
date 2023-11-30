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
import com.koch.ambeth.informationbus.persistence.setup.ISchemaFileProvider;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.jetty.JettyApplication;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.slf4j.Slf4jLogger;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.DatabaseProtocolResolver;
import lombok.SneakyThrows;
import org.burningwave.core.classes.Modules;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SQLStructure(schemaFileProvider = Main.HelloWorldSchemaFileProvider.class)
public class Main {
    private static Class<?> moduleClass = Module.class;
    private static Set<Object> allSet = new HashSet<>();
    private static Set<Object> everyOneSet = new HashSet<>();
    private static Set<Object> allUnnamedSet = new HashSet<>();

    public static void main(String[] args) throws Throwable {
        Modules.create().exportAllToAll();
        var dbContainer = new PostgreSQLContainer<>("postgres:15-alpine");
        dbContainer.start();

        LoggerFactory.setLoggerType(Slf4jLogger.class);
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

    @SneakyThrows
    public static void exportToAll(String fieldName, Object module, String pkgName) {
        var field = module.getClass().getDeclaredField(fieldName);
        Map<String, Set<?>> pckgForModule = (Map<String, Set<?>>) field.get(module);
        if (pckgForModule == null) {
            pckgForModule = new HashMap<>();
            field.set(module, pckgForModule);
        }
        pckgForModule.put(pkgName, allSet);
        if (fieldName.startsWith("exported")) {
            var method = moduleClass.getDeclaredMethod("addExportsToAll0", moduleClass, String.class);
            method.invoke(null, module, pkgName);
        }
    }

    public static class HelloWorldSchemaFileProvider implements ISchemaFileProvider {

        @Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol)
        protected String databaseProtocol;

        @Override
        public String[] getSchemaFiles() {
            return new String[] { "schema-" + databaseProtocol.replace(':', '_') + "/create_hello_world_tables.sql" };
        }
    }
}
