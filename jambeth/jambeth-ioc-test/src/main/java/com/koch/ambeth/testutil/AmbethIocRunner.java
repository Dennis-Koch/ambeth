package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-ioc-test
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
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.ioc.util.IPropertiesProvider;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.log.slf4j.Slf4jLogger;
import com.koch.ambeth.util.annotation.AnnotationInfo;
import com.koch.ambeth.util.annotation.IAnnotationInfo;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class AmbethIocRunner extends BlockJUnit4ClassRunner {
    protected static List<TestProperties> getAllTestProperties(Class<?> testClass, Method testMethod) {
        var testPropertiesList = findAnnotations(testClass, testMethod, TestPropertiesList.class, TestProperties.class);

        var allTestProperties = new LinkedHashMap<String, TestProperties>();

        var additionalProperties = new ArrayList<TestProperties>();

        for (int a = 0, size = testPropertiesList.size(); a < size; a++) {
            IAnnotationInfo<?> annotationInfo = testPropertiesList.get(a);
            Annotation testPropertiesItem = annotationInfo.getAnnotation();

            if (testPropertiesItem instanceof TestPropertiesList) {
                TestPropertiesList mtp = (TestPropertiesList) testPropertiesItem;
                for (TestProperties testProperties : mtp.value()) {
                    if (testProperties.name().isEmpty()) {
                        additionalProperties.add(testProperties);
                        continue;
                    }
                    allTestProperties.put(testProperties.name(), testProperties);
                }
            } else {
                TestProperties testProperties = (TestProperties) testPropertiesItem;
                if (testProperties.name().isEmpty()) {
                    additionalProperties.add(testProperties);
                    continue;
                }
                allTestProperties.put(testProperties.name(), testProperties);
            }
        }
        var list = allTestProperties.values();
        list.addAll(additionalProperties);
        return list;
    }

    @SneakyThrows
    public static void extendProperties(Class<?> testClass, Method testMethod, Properties props) {
        var allTestProperties = getAllTestProperties(testClass, testMethod);

        var additionalTestProperties = new ArrayList<TestProperties>();
        for (int a = 0, size = allTestProperties.size(); a < size; a++) {
            var testProperties = allTestProperties.get(a);
            var testPropertiesType = testProperties.type();
            if (testPropertiesType != null && !IPropertiesProvider.class.equals(testPropertiesType)) {
                additionalTestProperties.add(testProperties);
            }
            var testPropertiesFile = testProperties.file();
            if (testPropertiesFile != null && testPropertiesFile.length() > 0) {
                props.load(testPropertiesFile);
            }
            var testPropertyName = testProperties.name();
            var testPropertyValue = testProperties.value();
            if (testPropertyName != null && testPropertyName.length() > 0) {
                if (testPropertyValue != null) {
                    if (testProperties.overrideIfExists() || props.get(testPropertyName) == null) {
                        // Override intended
                        props.put(testPropertyName, testPropertyValue);
                    }
                }
            }
        }
        for (var testProperties : additionalTestProperties) {
            var propertiesProvider = testProperties.type().newInstance();
            propertiesProvider.fillProperties(props);
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Method method, Class<?>... annotationTypes) {
        ArrayList<IAnnotationInfo<?>> targetList = new ArrayList<>();
        findAnnotations(type, targetList, true, annotationTypes);

        if (method != null) {
            for (Class<?> annotationType : annotationTypes) {
                Annotation annotation = method.getAnnotation((Class<? extends Annotation>) annotationType);
                if (annotation != null) {
                    targetList.add(new AnnotationInfo<>(annotation, method));
                }
            }
        }
        return targetList;
    }

    @SuppressWarnings("unchecked")
    protected static void findAnnotations(Class<?> type, List<IAnnotationInfo<?>> targetList, boolean isFirst, Class<?>... annotationTypes) {
        if (type == null || Object.class.equals(type)) {
            return;
        }
        if (!type.isInterface()) {
            findAnnotations(type.getSuperclass(), targetList, false, annotationTypes);
        }
        for (Class<?> annotationType : annotationTypes) {
            Annotation annotation = type.getAnnotation((Class<? extends Annotation>) annotationType);
            if (annotation != null) {
                targetList.add(new AnnotationInfo<>(annotation, type));
            }
        }
        if (isFirst) {
            Class<?>[] interfaces = type.getInterfaces();
            for (Class<?> currInterface : interfaces) {
                for (Class<?> annotationType : annotationTypes) {
                    Annotation annotationOfInterface = currInterface.getAnnotation((Class<? extends Annotation>) annotationType);
                    if (annotationOfInterface != null) {
                        targetList.add(new AnnotationInfo<>(annotationOfInterface, currInterface));
                    }
                }
            }
        }
    }

    protected final ThreadLocal<Object> targetProxyTL = new ThreadLocal<>();
    protected boolean hasContextBeenRebuildForThisTest;
    protected boolean isRebuildContextForThisTestRecommended;

    protected IAmbethApplication ambeth;

    public AmbethIocRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected void finalize() throws Throwable {
        disposeContext();
        super.finalize();
    }

    public IServiceContext getBeanContext() {
        if (ambeth != null) {
            return ambeth.getApplicationContext();
        }
        return null;
    }

    public IServiceContext getRootContext() {
        if (ambeth != null) {
            return ambeth.getApplicationContext().getRoot();
        }
        return null;
    }

    public void cleanupThreadLocals() {
        getBeanContext().getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
    }

    public void rebuildContext() {
        rebuildContext(null);
    }

    public void disposeContext() {
        if (ambeth != null) {
            var bootstrapContext = ambeth.getApplicationContext().getRoot();
            IThreadLocalCleanupController tlCleanupController = bootstrapContext.getService(IThreadLocalCleanupController.class);
            ambeth.close();
            ambeth = null;
            tlCleanupController.cleanupThreadLocal();
        }
    }

    protected List<Class<? extends IInitializingModule>> buildTestModuleList(Method testMethod) {
        var testModulesList = findAnnotations(getTestClass().getJavaClass(), testMethod, TestModule.class);

        var moduleList = new ArrayList<Class<? extends IInitializingModule>>();
        for (var testModuleItem : testModulesList) {
            var testFrameworkModule = (TestModule) testModuleItem.getAnnotation();
            moduleList.addAll(testFrameworkModule.value());
        }
        return moduleList;
    }

    protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(Method testMethod) {
        var testFrameworkModulesList = findAnnotations(getTestClass().getJavaClass(), testMethod, TestFrameworkModule.class);

        var frameworkModuleList = new ArrayList<Class<? extends IInitializingModule>>();
        for (var testModuleItem : testFrameworkModulesList) {
            var testFrameworkModule = (TestFrameworkModule) testModuleItem.getAnnotation();
            frameworkModuleList.addAll(testFrameworkModule.value());
        }
        return frameworkModuleList;
    }

    @SuppressWarnings("unchecked")
    protected void rebuildContext(Method testMethod) {
        disposeContext();

        LoggerFactory.setLoggerType(Slf4jLogger.class);

        var props = new Properties(Properties.getApplication());
        var propertyFileKey = UtilConfigurationConstants.BootstrapPropertyFile;
        var bootstrapPropertyFile = props.getString(propertyFileKey);
        if (bootstrapPropertyFile == null) {
            propertyFileKey = UtilConfigurationConstants.BootstrapPropertyFile.toUpperCase();
            bootstrapPropertyFile = props.getString(propertyFileKey);
        }
        if (bootstrapPropertyFile != null) {
            LoggerFactory.getLogger(getClass(), props).info("Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "' found with value '" + bootstrapPropertyFile + "'");
            props.load(bootstrapPropertyFile, false);
        }
        extendPropertiesInstance(testMethod, props);

        var testClassLevelTestFrameworkModulesList = new LinkedHashSet<Class<? extends IInitializingModule>>();
        var testClassLevelTestModulesList = new LinkedHashSet<Class<? extends IInitializingModule>>();

        testClassLevelTestModulesList.addAll(buildTestModuleList(testMethod));
        testClassLevelTestFrameworkModulesList.addAll(buildFrameworkTestModuleList(testMethod));

        var frameworkModules = testClassLevelTestFrameworkModulesList.toArray(Class[]::new);
        var applicationModules = testClassLevelTestModulesList.toArray(Class[]::new);

        var rollback = FileUtil.pushCurrentTypeScope(getTestClass().getJavaClass());
        try {
            ambeth = Ambeth.createEmpty()
                           .withFrameworkModules(frameworkModules)
                           .withFrameworkModules(this::rebuildContextDetails)
                           .withApplicationModules(applicationModules)
                           .withProperties(props)
                           .start();
        } finally {
            rollback.rollback();
        }
    }

    protected void extendPropertiesInstance(Method testMethod, Properties props) {
        extendProperties(getTestClass().getJavaClass(), testMethod, props);
    }

    protected void rebuildContextDetails(IBeanContextFactory childContextFactory) {
        childContextFactory.registerExternalBean(new TestContext(this)).autowireable(ITestContext.class);
        Class<? extends ICleanupAfter> cleanupAfterType = getCleanupAfterType();
        if (cleanupAfterType != null) {
            childContextFactory.registerBean(cleanupAfterType).autowireable(ICleanupAfter.class);
        }
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        final Statement withAfterClasses = super.withAfterClasses(statement);

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                withAfterClasses.evaluate();
                disposeContext();
                System.gc();
            }
        };
    }

    protected Class<? extends ICleanupAfter> getCleanupAfterType() {
        return CleanupAfterIoc.class;
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, final Object target, Statement statement) {
        final Statement returningStatement = super.withAfters(method, target, statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                var beanContext = getBeanContext();
                Object targetProxy;
                if (IRunnerAware.class.isAssignableFrom(target.getClass())) {
                    targetProxy = beanContext.registerWithLifecycle(target).propertyValue("Runner", AmbethIocRunner.this).finish();
                } else {
                    targetProxy = beanContext.registerWithLifecycle(target).finish();
                }
                var oldTargetProxy = targetProxyTL.get();
                targetProxyTL.set(targetProxy);
                try {
                    returningStatement.evaluate();
                } catch (RuntimeException e) {
                    if (beanContext != null && beanContext.isRunning()) {
                        var props = beanContext.getService(IProperties.class);
                        LoggerFactory.getLogger(AmbethIocRunner.this.getClass(), props).error(e);
                    }
                    throw e;
                } finally {
                    targetProxyTL.set(oldTargetProxy);

                    if (beanContext != null) {
                        var cleanupAfter = beanContext.getService(ICleanupAfter.class, false);
                        if (cleanupAfter != null) {
                            cleanupAfter.cleanup();
                        }
                    }
                }
            }
        };
    }

    @Override
    protected Object createTest() throws Exception {
        Class<?> javaClass = getTestClass().getJavaClass();
        return javaClass.newInstance();
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
        final Statement statement = super.methodInvoker(method, test);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Object targetProxy = targetProxyTL.get();
                if (targetProxy != null) {
                    method.invokeExplosively(targetProxy);
                } else {
                    statement.evaluate();
                }
            }
        };
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (!hasContextBeenRebuildForThisTest) {
                    if (method == null || method.getAnnotation(Ignore.class) == null) {
                        List<IAnnotationInfo<?>> rebuildContextList = findAnnotations(getTestClass().getJavaClass(), TestRebuildContext.class);
                        if (!rebuildContextList.isEmpty()) {
                            boolean rebuildContext = ((TestRebuildContext) rebuildContextList.get(rebuildContextList.size() - 1).getAnnotation()).value();
                            if (rebuildContext) {
                                rebuildContext(method != null ? method.getMethod() : null);
                                hasContextBeenRebuildForThisTest = true;
                                isRebuildContextForThisTestRecommended = false;
                            }
                        }
                        if (method != null) {
                            Method targetMethod = method.getMethod();

                            if (targetMethod.isAnnotationPresent(TestModule.class) || targetMethod.isAnnotationPresent(TestFrameworkModule.class) ||
                                    targetMethod.isAnnotationPresent(TestPropertiesList.class) || targetMethod.isAnnotationPresent(TestProperties.class)) {
                                rebuildContext(method != null ? method.getMethod() : null);
                                hasContextBeenRebuildForThisTest = true;
                                isRebuildContextForThisTestRecommended = false;
                            }
                        }
                    }
                }
                if (ambeth == null || isRebuildContextForThisTestRecommended) {
                    rebuildContext(method != null ? method.getMethod() : null);
                }
                statement.evaluate();
            }
        };
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        IStateRollback rollback = FileUtil.pushCurrentTypeScope(getTestClass().getJavaClass());
        try {
            hasContextBeenRebuildForThisTest = false;
            isRebuildContextForThisTestRecommended = false;
            super.runChild(method, notifier);
        } finally {
            rollback.rollback();
        }
    }

    protected List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Class<?>... annotationTypes) {
        return findAnnotations(type, null, annotationTypes);
    }
}
