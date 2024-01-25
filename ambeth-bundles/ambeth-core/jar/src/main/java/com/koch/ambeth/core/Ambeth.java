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
import com.koch.ambeth.ioc.annotation.ApplicationModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Set;

public class Ambeth implements IAmbethConfiguration, IAmbethConfigurationIntern, IAmbethApplication {

    protected static final Set<IServiceContext> ACTIVE_APPLICATIONS = new HashSet<>();

    protected static final CheckedConsumer<IServiceContext> DISPOSE_HOOK = beanContext -> {
        synchronized (ACTIVE_APPLICATIONS) {
            ACTIVE_APPLICATIONS.remove(beanContext);
        }
    };

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            IServiceContext[] contexts;
            synchronized (ACTIVE_APPLICATIONS) {
                contexts = ACTIVE_APPLICATIONS.toArray(IServiceContext[]::new);
                ACTIVE_APPLICATIONS.clear();
            }
            for (var serviceContext : contexts) {
                try {
                    serviceContext.getRoot().dispose();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }));
    }

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

    @SneakyThrows
    protected static void setBundleModule(Class<? extends IBundleModule> bundleModule, Ambeth ambeth) {
        var bundleModuleInstance = bundleModule.newInstance();
        var bundleModules = bundleModuleInstance.getBundleModules();
        ambeth.withFrameworkModules(bundleModules);

        var bundleModuleInstances = bundleModuleInstance.getBundleModuleInstances();
        ambeth.withFrameworkModules(bundleModuleInstances);
    }

    protected final boolean scanForFrameworkModules;
    protected final boolean scanForApplicationModules;
    protected Properties properties = new Properties();
    protected boolean scanForPropertiesFile = true;
    protected IdentityLinkedSet<CheckedConsumer<IBeanContextFactory>> frameworkModuleDelegates = new IdentityLinkedSet<>();
    protected LinkedHashSet<Class<? extends IInitializingModule>> frameworkModules = new LinkedHashSet<>();
    protected IdentityLinkedSet<CheckedConsumer<IBeanContextFactory>> applicationModuleDelegates = new IdentityLinkedSet<>();
    protected LinkedHashSet<Class<? extends IInitializingModule>> applicationModules = new LinkedHashSet<>();
    protected LinkedHashMap<Class<?>, Object> autowiredFrameworkBeans = new LinkedHashMap<>();
    protected ClassLoader classLoader;
    private ArrayList<String> propertiesFiles = new ArrayList<>();
    private IServiceContext rootContext;

    private IServiceContext applicationContext;
    private boolean useCDI;

    private Ambeth(boolean scanForFrameworkModules, boolean scanForApplicationModules) {
        this.scanForFrameworkModules = scanForFrameworkModules;
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

    public IAmbethConfiguration withCDI() {
        useCDI = true;
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
    public IAmbethConfiguration withFrameworkModules(Class<? extends IInitializingModule>... frameworkModuleTypes) {
        frameworkModules.addAll(frameworkModuleTypes);
        return this;
    }

    @Override
    public IAmbethConfiguration withFrameworkModules(List<Class<? extends IInitializingModule>> frameworkModuleTypes) {
        frameworkModules.addAll(frameworkModuleTypes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public IAmbethConfigurationIntern withFrameworkModules(CheckedConsumer<IBeanContextFactory>... moduleDelegates) {
        frameworkModuleDelegates.addAll(moduleDelegates);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAmbethConfiguration withApplicationModules(Class<? extends IInitializingModule>... applicationModuleTypes) {
        applicationModules.addAll(applicationModuleTypes);
        return this;
    }

    @Override
    public IAmbethConfiguration withApplicationModules(List<Class<? extends IInitializingModule>> applicationModuleTypes) {
        applicationModules.addAll(applicationModuleTypes);
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
        var properties = new Properties(this.properties);
        if (scanForPropertiesFile) {
            Properties.loadBootstrapPropertyFile(properties);
        }
        for (int i = 0, size = propertiesFiles.size(); i < size; i++) {
            var filename = propertiesFiles.get(i);
            properties.load(filename);
        }
        if (classLoader != null) {
            properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
        } else {
            classLoader = properties.get(IocConfigurationConstants.ExplicitClassLoader);
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
                properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
            }
        }
        if (useCDI) {
            startCDI(andClose, properties);
            return;
        }
        IServiceContext currentApplicationContext = null;
        boolean success = false;
        var rollback = IClassLoaderProvider.pushClassLoader(classLoader);
        try {
            try {
                if (frameworkModules.remove(IocModule.class)) {
                    currentApplicationContext = BeanContextFactory.createBootstrap(properties, IocModule.class);
                } else {
                    currentApplicationContext = BeanContextFactory.createBootstrap(properties);
                }
                var allFrameworkModules = new HashSet<>(this.frameworkModules);
                var allApplicationModules = new HashSet<>(this.applicationModules);
                scanForModules(currentApplicationContext, allFrameworkModules, allApplicationModules);

                var ambethApplication = this;
                currentApplicationContext = currentApplicationContext.createService("framework", childContextFactory -> {
                    childContextFactory.registerExternalBean(ambethApplication).autowireable(IAmbethApplication.class);

                    for (var moduleDelegate : frameworkModuleDelegates) {
                        if (moduleDelegate == null) {
                            continue;
                        }
                        moduleDelegate.accept(childContextFactory);
                    }
                    for (var autowiredFrameworkBean : autowiredFrameworkBeans) {
                        var typeToPublish = autowiredFrameworkBean.getKey();
                        var externalBean = autowiredFrameworkBean.getValue();
                        childContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
                    }
                }, allFrameworkModules.toArray(Class[]::new));

                if (!allApplicationModules.isEmpty() || !applicationModuleDelegates.isEmpty()) {
                    currentApplicationContext = currentApplicationContext.createService("application", childContextFactory -> {
                        for (var moduleDelegate : applicationModuleDelegates) {
                            moduleDelegate.accept(childContextFactory);
                        }
                    }, allApplicationModules.toArray(Class[]::new));
                }
                success = true;
            } finally {
                if (!success && currentApplicationContext != null) {
                    currentApplicationContext.getRoot().dispose();
                }
            }
        } finally {
            rollback.rollback();
        }
        this.applicationContext = currentApplicationContext;
        this.rootContext = currentApplicationContext.getRoot();
    }

    protected void startCDI(boolean andClose, Properties properties) {
        //        AnnotationConfigApplicationContext currentApplicationContext = null;
        //        boolean success = false;
        //        var rollback = IClassLoaderProvider.pushClassLoader(classLoader);
        //        try {
        //            try {
        //                var ctx = new AnnotationConfigApplicationContext();
        //                var env = new StandardEnvironment();
        //                var propertySources = env.getPropertySources();
        //                propertySources.addFirst(new PropertiesPropertySource("ambeth-props", properties));
        //                ctx.setEnvironment(env);
        //                ctx.register(properties);
        //
        //                ctx.register(AmbethBootstrapSpringConfig.class);
        //                if (frameworkModules.remove(IocModule.class)) {
        //                    ctx.register(IocModule.class);
        //                    ctx.refresh();
        //                } else {
        //                    currentApplicationContext = BeanContextFactory.createBootstrap(properties);
        //                }
        //                var allFrameworkModules = new HashSet<>(this.frameworkModules);
        //                var allApplicationModules = new HashSet<>(this.applicationModules);
        //                scanForModules(currentApplicationContext, allFrameworkModules, allApplicationModules);
        //
        //                var ambethApplication = this;
        //                currentApplicationContext = currentApplicationContext.createService("framework", childContextFactory -> {
        //                    childContextFactory.registerExternalBean(ambethApplication).autowireable(IAmbethApplication.class);
        //
        //                    for (var moduleDelegate : frameworkModuleDelegates) {
        //                        if (moduleDelegate == null) {
        //                            continue;
        //                        }
        //                        moduleDelegate.accept(childContextFactory);
        //                    }
        //                    for (var autowiredFrameworkBean : autowiredFrameworkBeans) {
        //                        var typeToPublish = autowiredFrameworkBean.getKey();
        //                        var externalBean = autowiredFrameworkBean.getValue();
        //                        childContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
        //                    }
        //                }, allFrameworkModules.toArray(Class[]::new));
        //
        //                if (!allApplicationModules.isEmpty() || !applicationModuleDelegates.isEmpty()) {
        //                    currentApplicationContext = currentApplicationContext.createService("application", childContextFactory -> {
        //                        for (var moduleDelegate : applicationModuleDelegates) {
        //                            moduleDelegate.accept(childContextFactory);
        //                        }
        //                    }, allApplicationModules.toArray(Class[]::new));
        //                }
        //                success = true;
        //            } finally {
        //                if (!success && currentApplicationContext != null) {
        //                    currentApplicationContext.getRoot().dispose();
        //                }
        //            }
        //        } finally {
        //            rollback.rollback();
        //        }
        //        this.applicationContext = currentApplicationContext;
        //        this.rootContext = currentApplicationContext.getRoot();
    }

    @Override
    public boolean isClosed() {
        return rootContext.isDisposing() || rootContext.isDisposed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IServiceContext getApplicationContext() {
        return applicationContext;
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
    public <T> void registerFrameworkBean(T instance, Class<T> autowiring) {
        autowiredFrameworkBeans.put(autowiring, instance);
    }

    protected void scanForModules(IServiceContext currentContext, Set<Class<? extends IInitializingModule>> frameworkModules, Set<Class<? extends IInitializingModule>> applicationModules) {
        if (!scanForFrameworkModules && !scanForApplicationModules) {
            return;
        }
        var classpathScanner = currentContext.registerBean(ConfigurableClasspathScanner.class).propertyValue("AutowiredInstances", autowiredFrameworkBeans).finish();
        try {
            if (scanForFrameworkModules) {
                classpathScanner.scanClassesAnnotatedWith(FrameworkModule.class).stream().map(frameworkModule -> {
                    if (!(IInitializingModule.class.isAssignableFrom(frameworkModule))) {
                        throw new IllegalStateException(
                                "Class annotated with " + FrameworkModule.class.getName() + " but does not implement " + IInitializingModule.class.getName() + ": " + frameworkModule.getName());
                    }
                    return (Class<? extends IInitializingModule>) frameworkModule;
                }).forEach(frameworkModules::add);
            }
            if (scanForApplicationModules) {
                classpathScanner.scanClassesAnnotatedWith(ApplicationModule.class).stream().map(applicationModule -> {
                    if (!(IInitializingModule.class.isAssignableFrom(applicationModule))) {
                        throw new IllegalStateException(
                                "Class annotated with " + ApplicationModule.class.getName() + " but does not implement " + IInitializingModule.class.getName() + ": " + applicationModule.getName());
                    }
                    return (Class<? extends IInitializingModule>) applicationModule;
                }).forEach(applicationModules::add);
            }
        } finally {
            classpathScanner.dispose();
        }
    }
}
