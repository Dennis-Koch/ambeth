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

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.informationbus.persistence.InformationBusWithPersistence;
import com.koch.ambeth.informationbus.persistence.setup.AmbethPersistenceSetup;
import com.koch.ambeth.informationbus.persistence.setup.DataSetupExecutor;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.transaction.ILightweightTransaction;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class RebuildSchema {
    /**
     * To fully rebuild schema and data of an application server run with the following program
     * arguments:
     * <p>
     * Local App Server: <code>property.file=src/main/environment/test/iqdf-ui.properties</code>
     * <p>
     * Dev Server (Caution for concurrent users/developers/testers):
     * <code>property.file=src/main/environment/dev/iqdf-ui.properties</code>
     * <p>
     * Demo Server (Caution for concurrent users/developers/testers):
     * <code>property.file=src/main/environment/demo/iqdf-ui.properties</code>
     *
     * @param args
     */
    @SneakyThrows
    public static void main(final String[] args, Class<?> testClass, String recommendedPropertyFileName) {
        var props = new Properties(Properties.getApplication());
        props.fillWithCommandLineArgs(args);

        var propertyFileKey = UtilConfigurationConstants.BootstrapPropertyFile;
        var bootstrapPropertyFile = props.getString(propertyFileKey);
        if (bootstrapPropertyFile == null) {
            propertyFileKey = UtilConfigurationConstants.BootstrapPropertyFile.toUpperCase();
            bootstrapPropertyFile = props.getString(propertyFileKey);
        }
        if (bootstrapPropertyFile != null) {
            System.out.println("Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "' found with value '" + bootstrapPropertyFile + "'");
            props.load(bootstrapPropertyFile, false);
        }
        props.put(PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, true);
        if (props.get("ambeth.log.level") == null) {
            props.put("ambeth.log.level", "INFO");
        }
        // intentionally refill with args again
        props.fillWithCommandLineArgs(args);

        Ambeth.createEmptyBundle(InformationBusWithPersistence.class).withProperties(props).withoutPropertiesFileSearch().start();

        var ambethPersistenceSetup = new AmbethPersistenceSetup(testClass) {
            public void extendPropertiesInstance(Method annotatedMethod, Properties props) {
                super.extendPropertiesInstance(annotatedMethod, props);

                // intentionally refill with args again
                props.fillWithCommandLineArgs(args);
            }
        };
        try {
            ambethPersistenceSetup.rebuildSchemaContext();
        } catch (BeanContextInitException e) {
            if (!e.getMessage().startsWith("Could not resolve mandatory environment property 'database.schema.name'")) {
                throw e;
            }
            var ex = new IllegalArgumentException("Please specify the corresponding property file e.g.:\n" + UtilConfigurationConstants.BootstrapPropertyFile + "=" + recommendedPropertyFileName);
            ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            throw ex;
        }
        ambethPersistenceSetup.rebuildStructure();
        ambethPersistenceSetup.rebuildData(null);

        var rollback = DataSetupExecutor.pushAutoRebuildData(Boolean.TRUE);
        try {
            try (var ambeth = Ambeth.createEmptyBundle(InformationBusWithPersistence.class)
                                    .withProperties(props)
                                    .withFrameworkModules(ambethPersistenceSetup.buildFrameworkTestModuleList(null))
                                    .start()) {
                var beanContext = ambeth.getApplicationContext();

                ambethPersistenceSetup.rollbackConnection();
                beanContext.getService(ILightweightTransaction.class).runInTransaction(() -> {
                    // Intended blank
                });
            }
        } finally {
            rollback.rollback();
        }
    }
}
