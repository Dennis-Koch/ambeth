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

import com.koch.ambeth.informationbus.persistence.setup.AmbethPersistenceSetup;
import com.koch.ambeth.informationbus.persistence.setup.DataSetupExecutor;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLDataList;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.slf4j.Slf4jLogger;
import com.koch.ambeth.merge.ChangeControllerState;
import com.koch.ambeth.merge.changecontroller.IChangeController;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.security.TestAuthentication;
import com.koch.ambeth.security.server.SecurityFilterInterceptor;
import com.koch.ambeth.security.server.SecurityFilterInterceptor.SecurityMethodMode;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.transaction.ILightweightTransaction;
import com.koch.ambeth.xml.DefaultXmlWriter;
import lombok.SneakyThrows;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.lang.reflect.Method;
import java.util.List;

/**
 * TODO: Handle test methods which change the structure
 */
public class AmbethInformationBusWithPersistenceRunner extends AmbethInformationBusRunner {
    protected static final String MEASUREMENT_BEAN = "measurementBean";

    protected final StringBuilder measurementXML = new StringBuilder();
    protected final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null, new ImmutableTypeSet());
    protected boolean isRebuildDataForThisTestRecommended;

    /**
     * Flag which is set to true after the structure was build.
     */
    private boolean isStructureRebuildAlreadyHandled = false;
    /**
     * Flag which is set to true after the first test method was executed.
     */
    private boolean isFirstTestMethodAlreadyExecuted;
    /**
     * Flag which is set if the last test method has triggered a context rebuild.
     */
    private boolean lastMethodTriggersContextRebuild;

    private JdbcDatabaseContainer<?> jdbcDatabaseContainer;

    private AmbethPersistenceSetup ambethPersistenceSetup;

    public AmbethInformationBusWithPersistenceRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(Method testMethod) {
        List<Class<? extends IInitializingModule>> frameworkTestModuleList = super.buildFrameworkTestModuleList(testMethod);
        frameworkTestModuleList.addAll(getAmbethPersistenceSetup().buildFrameworkTestModuleList(testMethod));
        return frameworkTestModuleList;
    }

    /**
     * Due to a lot of new DB connections during tests /dev/random on CI servers may run low.
     */
    private void checkOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            // the 3 '/' are important to make it an URL
            System.setProperty("java.security.egd", "file:///dev/urandom");
        }
    }

    @SneakyThrows
    @SuppressWarnings("resource")
    @Override
    protected void extendPropertiesInstance(Method testMethod, Properties props) {
        super.extendPropertiesInstance(testMethod, props);

        getAmbethPersistenceSetup().extendPropertiesInstance(testMethod, props);
    }

    @Override
    protected void finalize() throws Throwable {
        if (ambethPersistenceSetup != null) {
            ambethPersistenceSetup.close();
        }
        super.finalize();
    }

    protected ILogger getLog() {
        var schemaContext = getAmbethPersistenceSetup().getOrCreateSchemaContext();
        return schemaContext.getService(ILoggerCache.class).getCachedLogger(schemaContext, AmbethInformationBusWithPersistenceRunner.class);
    }

    /**
     * @return Flag if data rebuild is demanded (checks the test class annotation or returns the
     * default value).
     */
    protected boolean isDataRebuildDemanded() {
        var result = true; // default value if no annotation is found
        var sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
        if (!sqlDataRebuilds.isEmpty()) {
            var topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
            result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).value();
        }
        return result;
    }

    /**
     * @return Flag if a truncate of the data tables (on test class level) is demanded (checks the
     * test class annotation or returns the default value).
     */
    protected boolean isTruncateOnClassDemanded() {
        var result = true; // default value
        var sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
        if (!sqlDataRebuilds.isEmpty()) {
            var topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
            if (topDataRebuild.getAnnotation() instanceof SQLDataRebuild) {
                result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).truncateOnClass();
            }
        }
        return result;
    }

    protected void logMeasurement(final String name, final Object value) {
        var elementName = name.replaceAll(" ", "_").replaceAll("\\.", "_").replaceAll("\\(", ":").replaceAll("\\)", ":");
        xmlWriter.writeOpenElement(elementName);
        xmlWriter.writeEscapedXml(value.toString());
        xmlWriter.writeCloseElement(elementName);
    }

    @Override
    protected org.junit.runners.model.Statement methodBlock(final FrameworkMethod frameworkMethod) {
        var statement = super.methodBlock(frameworkMethod);
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                var doContextRebuild = false;
                var method = frameworkMethod.getMethod();
                var ambethPersistenceSetup = getAmbethPersistenceSetup();
                var doStructureRebuild = !isStructureRebuildAlreadyHandled && ambethPersistenceSetup.hasStructureAnnotation();
                var methodTriggersContextRebuild =
                        method.isAnnotationPresent(TestModule.class) || method.isAnnotationPresent(TestProperties.class) || method.isAnnotationPresent(TestPropertiesList.class);
                var beanContext = getBeanContext();
                doContextRebuild = beanContext == null || beanContext.isDisposed() || doStructureRebuild || methodTriggersContextRebuild || lastMethodTriggersContextRebuild;
                lastMethodTriggersContextRebuild = methodTriggersContextRebuild;
                var doDataRebuild = isDataRebuildDemanded();
                if (!doDataRebuild) // handle the special cases for SQLDataRebuild=false
                {
                    // If SQL data on class level -> run data SQL before the first test method
                    if (!isFirstTestMethodAlreadyExecuted) {
                        doDataRebuild = !findAnnotations(getTestClass().getJavaClass(), SQLDataList.class, SQLData.class).isEmpty();
                    }
                }
                var doAddAdditionalMethodData = false; // Flag if SQL method data should be
                // inserted
                // (without deleting
                // existing database entries)
                if (!doDataRebuild) // included in data rebuild -> only check if data rebuild isn't
                // done
                {
                    doAddAdditionalMethodData = method.isAnnotationPresent(SQLData.class) || method.isAnnotationPresent(SQLDataList.class);
                }

                if (doStructureRebuild) {
                    getAmbethPersistenceSetup().rebuildStructure();
                }
                if (doDataRebuild) {
                    ambethPersistenceSetup.rebuildData(method);
                }
                if (doAddAdditionalMethodData) {
                    ambethPersistenceSetup.executeAdditionalDataRunnables(method);
                }
                // Do context rebuild after the database changes have been made because the beans
                // may access
                // the data e.g.
                // in their afterStarted method
                isRebuildContextForThisTestRecommended = doContextRebuild;
                isFirstTestMethodAlreadyExecuted = true;
                isRebuildDataForThisTestRecommended = doDataRebuild;

                statement.evaluate();
            }
        };
    }

    @Override
    protected org.junit.runners.model.Statement methodInvoker(final FrameworkMethod method, Object test) {
        var parentStatement = AmbethInformationBusWithPersistenceRunner.super.methodInvoker(method, test);
        var statement = new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                var beanContext = getBeanContext();
                var dataSetup = beanContext.getParent().getService(IDataSetup.class, false);
                if (dataSetup != null) {
                    dataSetup.refreshEntityReferences();
                }
                parentStatement.evaluate();
            }
        };
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                var beanContext = getBeanContext();
                if (isRebuildDataForThisTestRecommended) {
                    beanContext.getService(DataSetupExecutor.class).rebuildData();
                    isRebuildDataForThisTestRecommended = false;
                }
                var securityActive = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(MergeConfigurationConstants.SecurityActive, "false"));
                if (!securityActive) {
                    statement.evaluate();
                    return;
                }

                var changeControllerState = method.getAnnotation(ChangeControllerState.class);

                var changeControllerActiveTest = false;
                var changeController = beanContext.getService(IChangeController.class, false);
                if (changeControllerState != null) {
                    if (changeController != null) {
                        var conversionHelper = beanContext.getService(IConversionHelper.class);
                        var active = conversionHelper.convertValueToType(Boolean.class, changeControllerState.active());
                        if (Boolean.TRUE.equals(active)) {
                            changeControllerActiveTest = true;
                        }
                    }

                }
                var changeControllerActive = changeControllerActiveTest;

                var authentication = method.getAnnotation(TestAuthentication.class);
                if (authentication == null) {
                    var testClass = getTestClass().getJavaClass();
                    authentication = testClass.getAnnotation(TestAuthentication.class);
                }
                if (authentication == null) {
                    statement.evaluate();
                    return;
                }
                var scope = new StringSecurityScope(authentication.scope());

                var behaviour = new IMethodLevelBehavior<SecurityMethodMode>() {
                    private final SecurityMethodMode mode = new SecurityMethodMode(SecurityContextType.AUTHENTICATED, -1, -1, null, -1, scope);

                    @Override
                    public SecurityMethodMode getBehaviourOfMethod(Method method) {
                        return mode;
                    }

                    @Override
                    public SecurityMethodMode getDefaultBehaviour() {
                        return mode;
                    }
                };

                var interceptor = beanContext.registerBean(SecurityFilterInterceptor.class)
                                             .propertyValue(SecurityFilterInterceptor.P_METHOD_LEVEL_BEHAVIOUR, behaviour)
                                             .propertyValue("Target", statement)
                                             .finish();
                var stmt = (org.junit.runners.model.Statement) beanContext.getService(IProxyFactory.class).createProxy(new Class<?>[] { org.junit.runners.model.Statement.class }, interceptor);
                var securityContextHolder = beanContext.getService(ISecurityContextHolder.class);
                var fAuthentication = authentication;
                var rollback = StateRollback.chain(chain -> {
                    chain.append(securityContextHolder.pushAuthentication(new DefaultAuthentication(fAuthentication.name(), fAuthentication.password().toCharArray(), PasswordType.PLAIN)));
                    if (changeControllerActive && changeController != null) {
                        chain.append(changeController.pushRunWithoutEDBL());
                    }
                });
                try {
                    stmt.evaluate();
                } finally {
                    rollback.rollback();
                }
            }
        };
    }

    @SneakyThrows
    @Override
    protected void rebuildContext(Method testMethod) {
        if (testMethod == null) {
            return;
        }
        if (isRebuildDataForThisTestRecommended) {
            var rollback = DataSetupExecutor.pushAutoRebuildData(Boolean.TRUE);
            try {
                super.rebuildContext(testMethod);
            } finally {
                rollback.rollback();
                isRebuildDataForThisTestRecommended = false;
            }
        } else {
            super.rebuildContext(testMethod);
        }
        getAmbethPersistenceSetup().rollbackConnection();
        var beanContext = getBeanContext();
        beanContext.getService(ILightweightTransaction.class).runInTransaction(() -> {
            // Intended blank
        });
    }

    @Override
    protected void rebuildContextDetails(final IBeanContextFactory childContextFactory) {
        super.rebuildContextDetails(childContextFactory);

        childContextFactory.registerBean(MEASUREMENT_BEAN, Measurement.class).propertyValue("TestClassName", getTestClass().getJavaClass()).autowireable(IMeasurement.class);
    }

    @SneakyThrows
    public void rebuildData() {
        getAmbethPersistenceSetup().rebuildData(null);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        isRebuildDataForThisTestRecommended = false;
        super.runChild(method, notifier);
    }

    public void setDoExecuteStrict(boolean doExecuteStrict) {
        getAmbethPersistenceSetup().setDoExecuteStrict(doExecuteStrict);
    }

    @Override
    protected org.junit.runners.model.Statement withAfterClasses(final org.junit.runners.model.Statement statement) {
        var resultStatement = super.withAfterClasses(statement);
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    resultStatement.evaluate();
                    try {
                        var ambethPersistenceSetup = getAmbethPersistenceSetup();
                        // After all test methods of the test class have been executed we probably have to delete the test data
                        ambethPersistenceSetup.dropTestUserOrSchema();
                    } finally {
                        ambethPersistenceSetup.dropConnection();
                        ambethPersistenceSetup.dropSchemaContext();
                    }
                } finally {
                    if (jdbcDatabaseContainer != null) {
                        jdbcDatabaseContainer.close();
                        jdbcDatabaseContainer = null;
                    }
                }
            }
        };
    }

    @Override
    protected org.junit.runners.model.Statement withBeforeClasses(org.junit.runners.model.Statement statement) {
        checkOS();
        return super.withBeforeClasses(new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                //                if (System.getProperties().getProperty(PersistenceJdbcConfigurationConstants.DatabaseConnection) == null) {
                //                    if (jdbcDatabaseContainer == null) {
                //                        jdbcDatabaseContainer = new PostgreSQLContainer<>("postgres:15-alpine").withReuse(true).withLabel("scope", AmbethInformationBusWithPersistenceRunner.class
                //                        .getName());
                //                        jdbcDatabaseContainer.start();
                //                    }
                //                }
                //                System.getProperties().putIfAbsent(IocConfigurationConstants.TrackDeclarationTrace, "true");
                //                if (jdbcDatabaseContainer != null) {
                //                    System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseConnection, jdbcDatabaseContainer.getJdbcUrl());
                //                    System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseName, jdbcDatabaseContainer.getDatabaseName());
                //                    System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseUser, jdbcDatabaseContainer.getUsername());
                //                    System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabasePass, jdbcDatabaseContainer.getPassword());
                //                    System.getProperties().putIfAbsent(PersistenceJdbcConfigurationConstants.DatabaseSchemaName, jdbcDatabaseContainer.getUsername());
                //                    DatabaseProtocolResolver.enrichWithDatabaseProtocol(System.getProperties());
                //                }
                statement.evaluate();
            }
        });
    }

    protected synchronized AmbethPersistenceSetup getAmbethPersistenceSetup() {
        if (ambethPersistenceSetup == null) {
            LoggerFactory.setLoggerType(Slf4jLogger.class);

            var props = new Properties(Properties.getApplication());
            Properties.loadBootstrapPropertyFile(props);

            ambethPersistenceSetup = new AmbethPersistenceSetup(getTestClass().getJavaClass()) {
                @Override
                protected ILogger getLog() {
                    return AmbethInformationBusWithPersistenceRunner.this.getLog();
                }

                @Override
                protected boolean isTruncateOnClassDemanded() {
                    return AmbethInformationBusWithPersistenceRunner.this.isTruncateOnClassDemanded();
                }
            }.withProperties(props);
        }
        return ambethPersistenceSetup;
    }
}
