package com.koch.ambeth.core;

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.core.start.ConfigurableClasspathScanner;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.core.start.IAmbethConfiguration;
import com.koch.ambeth.core.start.IAmbethConfigurationExtension;
import com.koch.ambeth.core.start.IAmbethConfigurationIntern;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;

import java.util.List;

public class Ambeth implements IAmbethConfiguration, IAmbethConfigurationIntern, IAmbethApplication {
    /**
     * Creates an Ambeth context and scans for Ambeth and application modules.
     *
     * @return Configuration object
     */
    public static IAmbethConfiguration createDefault() {
        Ambeth ambeth = new Ambeth(true, true);
        return ambeth;
    }

    /**
     * Creates an Ambeth context for a specific Ambeth bundle and scans for application modules.
     *
     * @return Configuration object
     */
    public static IAmbethConfiguration createBundle(Class<? extends IBundleModule> bundleModule) {
        Ambeth ambeth = new Ambeth(false, true);
        setBundleModule(bundleModule, ambeth);
        return ambeth;
    }

    /**
     * Creates an Ambeth context for a specific Ambeth bundle and does not scan for any modules.
     *
     * @return Configuration object
     */
    public static IAmbethConfiguration createEmptyBundle(Class<? extends IBundleModule> bundleModule) {
        Ambeth ambeth = new Ambeth(false, false);
        setBundleModule(bundleModule, ambeth);
        return ambeth;
    }

    /**
     * Creates an Ambeth context without any Ambeth or application modules.
     *
     * @return Configuration object
     */
    public static IAmbethConfiguration createEmpty() {
        Ambeth ambeth = new Ambeth(false, false);
        return ambeth;
    }

    protected static void setBundleModule(Class<? extends IBundleModule> bundleModule, Ambeth ambeth) {
        try {
            IBundleModule bundleModuleInstance = bundleModule.newInstance();
            Class<? extends IInitializingModule>[] bundleModules = bundleModuleInstance.getBundleModules();
            ambeth.withAmbethModules(bundleModules);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected final boolean scanForAmbethModules;
    protected final boolean scanForApplicationModules;
    protected Properties properties = new Properties();
    protected boolean scanForPropertiesFile = true;
    protected IdentityLinkedSet<CheckedConsumer<IBeanContextFactory>> ambethModuleDelegates = new IdentityLinkedSet<>();
    protected LinkedHashSet<Class<?>> ambethModules = new LinkedHashSet<>();
    protected IdentityLinkedSet<CheckedConsumer<IBeanContextFactory>> applicationModuleDelegates = new IdentityLinkedSet<>();
    protected LinkedHashSet<Class<?>> applicationModules = new LinkedHashSet<>();
    protected LinkedHashMap<Class<?>, Object> autowiredInstances = new LinkedHashMap<>();
    protected ClassLoader classLoader;
    private ArrayList<String> propertiesFiles = new ArrayList<>();
    private IServiceContext rootContext;

    private IServiceContext serviceContext;

    private Ambeth(boolean scanForAmbethModules, boolean scanForApplicationModules) {
        this.scanForAmbethModules = scanForAmbethModules;
        this.scanForApplicationModules = scanForApplicationModules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withProperties(IProperties properties) {
        this.properties.load(properties);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withProperties(java.util.Properties properties) {
        this.properties.load(properties);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withProperty(String name, String value) {
        properties.putString(name, value);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withPropertiesFile(String name) {
        propertiesFiles.add(name);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withoutPropertiesFileSearch() {
        scanForPropertiesFile = false;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withArgs(String... args) {
        properties.fillWithCommandLineArgs(args);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withClassLoader(ClassLoader classLoader) {
        ParamChecker.assertParamNotNull(classLoader, "classLoader");
        this.classLoader = classLoader;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withAmbethModules(Class<?>... modules) {
        ambethModules.addAll(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public IAmbethConfigurationIntern withAmbethModules(CheckedConsumer<IBeanContextFactory>... moduleDelegates) {
        ambethModuleDelegates.addAll(moduleDelegates);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withApplicationModules(Class<?>... modules) {
        applicationModules.addAll(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public IAmbethConfigurationIntern withApplicationModules(CheckedConsumer<IBeanContextFactory>... moduleDelegates) {
        applicationModuleDelegates.addAll(moduleDelegates);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends IAmbethConfigurationExtension> E withExtension(Class<E> extensionType) {
        try {
            E extension = extensionType.newInstance();
            extension.setAmbethConfiguration(this);
            return extension;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethApplication start() {
        startInternal(false);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAndClose() {
        startInternal(true);
    }

    protected void startInternal(boolean andClose) {
        Properties properties = new Properties(Properties.getApplication());
        if (scanForPropertiesFile) {
            Properties.loadBootstrapPropertyFile(properties);
        }
        properties.load(this.properties);
        for (int i = 0, size = propertiesFiles.size(); i < size; i++) {
            String filename = propertiesFiles.get(i);
            properties.load(filename);
        }
        if (classLoader != null) {
            properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
        } else {
            classLoader = (ClassLoader) properties.get(IocConfigurationConstants.ExplicitClassLoader);
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
                properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
            }
        }
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            if (ambethModules.remove(IocModule.class)) {
                rootContext = BeanContextFactory.createBootstrap(properties, IocModule.class);
            } else {
                rootContext = BeanContextFactory.createBootstrap(properties);
            }

            if (andClose) {
                registerShutdownHook();
            }

            scanForModules();

            final IAmbethApplication ambethApplication = this;
            var frameworkContext = rootContext.createService(childContextFactory -> {
                childContextFactory.registerExternalBean(ambethApplication).autowireable(IAmbethApplication.class);

                for (var moduleDelegate : ambethModuleDelegates) {
                    if (moduleDelegate == null) {
                        continue;
                    }
                    CheckedConsumer.invoke(moduleDelegate, childContextFactory);
                }
                for (var autowiring : autowiredInstances) {
                    var typeToPublish = autowiring.getKey();
                    var externalBean = autowiring.getValue();
                    childContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
                }
            }, ambethModules.toArray(Class.class));

            if (!applicationModules.isEmpty() || !applicationModuleDelegates.isEmpty()) {
                serviceContext = frameworkContext.createService(childContextFactory -> {
                    for (var moduleDelegate : applicationModuleDelegates) {
                        CheckedConsumer.invoke(moduleDelegate, childContextFactory);
                    }
                }, applicationModules.toArray(Class.class));
            } else {
                serviceContext = frameworkContext;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IServiceContext getApplicationContext() {
        return serviceContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        rootContext.dispose();
    }

    /**
     * Internal method for {@link IAmbethConfigurationExtension}s. This way they can hook bean
     * instances deep in the start process.
     *
     * @param instance   Bean instance to add to the framework and classpath scanner contexts
     * @param autowiring Type to autowire the bean to
     */
    public <T> void registerBean(T instance, Class<T> autowiring) {
        autowiredInstances.put(autowiring, instance);
    }

    protected void registerShutdownHook() {
        final IServiceContext rootContext = this.rootContext;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                rootContext.dispose();
            }
        }));
    }

    protected void scanForModules() {
        if (!scanForAmbethModules && !scanForApplicationModules) {
            return;
        }

        ConfigurableClasspathScanner classpathScanner = rootContext.registerBean(ConfigurableClasspathScanner.class).propertyValue("AutowiredInstances", autowiredInstances).finish();
        try {
            if (scanForAmbethModules) {
                List<Class<?>> ambethModules = classpathScanner.scanClassesAnnotatedWith(FrameworkModule.class);
                this.ambethModules.addAll(ambethModules);
            }
            if (scanForApplicationModules) {
                // TODO replace with @ApplicationModule and mark @BootstrapModule as deprecated
                List<Class<?>> applicationModules = classpathScanner.scanClassesAnnotatedWith(BootstrapModule.class);
                this.applicationModules.addAll(applicationModules);
            }
        } finally {
            classpathScanner.dispose();
        }
    }
}
