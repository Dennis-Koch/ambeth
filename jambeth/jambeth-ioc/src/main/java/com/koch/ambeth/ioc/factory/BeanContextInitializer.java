package com.koch.ambeth.ioc.factory;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.ioc.BeanMonitoringSupport;
import com.koch.ambeth.ioc.IBeanInstantiationProcessor;
import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IServiceContextIntern;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.IStartingModule;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.BeanInstanceConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.exception.BeanContextDeclarationException;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.ioc.hierarchy.SearchType;
import com.koch.ambeth.ioc.link.AbstractLinkContainer;
import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.ioc.proxy.CallingProxyPostProcessor;
import com.koch.ambeth.ioc.proxy.Self;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.SystemUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.ILinkedSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import lombok.SneakyThrows;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class BeanContextInitializer implements IBeanContextInitializer, IInitializingBean {
    protected static final HashSet<Class<?>> primitiveSet = new HashSet<>(0.5f);
    protected static final IdentityHashMap<PrecedenceType, Integer> precedenceOrder = new IdentityHashMap<>(0.5f);
    // Intentionally no SensitiveThreadLocal. Usage will alLways be cleaned up immediately
    protected static final ThreadLocal<BeanContextInit> currentBeanContextInitTL = new ThreadLocal<>();

    static {
        primitiveSet.add(Integer.class);
        primitiveSet.add(Integer.TYPE);
        primitiveSet.add(Long.class);
        primitiveSet.add(Long.TYPE);
        primitiveSet.add(Double.class);
        primitiveSet.add(Double.TYPE);
        primitiveSet.add(Float.class);
        primitiveSet.add(Float.TYPE);
        primitiveSet.add(Short.class);
        primitiveSet.add(Short.TYPE);
        primitiveSet.add(Character.class);
        primitiveSet.add(Character.TYPE);
        primitiveSet.add(Byte.class);
        primitiveSet.add(Byte.TYPE);
        primitiveSet.add(Boolean.class);
        primitiveSet.add(Boolean.TYPE);
        primitiveSet.add(String.class);
        primitiveSet.add(Class.class);
        primitiveSet.add(void.class);
        primitiveSet.add(BigInteger.class);
        primitiveSet.add(BigDecimal.class);
        primitiveSet.add(Object.class);

        precedenceOrder.put(PrecedenceType.LOWEST, Integer.valueOf(6));
        precedenceOrder.put(PrecedenceType.LOWER, Integer.valueOf(5));
        precedenceOrder.put(PrecedenceType.LOW, Integer.valueOf(4));
        precedenceOrder.put(PrecedenceType.MEDIUM, Integer.valueOf(3));
        precedenceOrder.put(PrecedenceType.DEFAULT, Integer.valueOf(3));
        precedenceOrder.put(PrecedenceType.HIGH, Integer.valueOf(2));
        precedenceOrder.put(PrecedenceType.HIGHER, Integer.valueOf(1));
        precedenceOrder.put(PrecedenceType.HIGHEST, Integer.valueOf(0));
    }

    public static IBeanContextFactory getCurrentBeanContextFactory() {
        BeanContextInit beanContextInit = currentBeanContextInitTL.get();
        if (beanContextInit == null) {
            return null;
        }
        return beanContextInit.beanContextFactory;
    }

    public static IServiceContext getCurrentBeanContext() {
        BeanContextInit beanContextInit = currentBeanContextInitTL.get();
        if (beanContextInit == null) {
            return null;
        }
        return beanContextInit.beanContext;
    }

    protected IPropertyInfoProvider propertyInfoProvider;
    protected IThreadLocalObjectCollector objectCollector;
    protected CallingProxyPostProcessor callingProxyPostProcessor;
    protected IConversionHelper conversionHelper;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(callingProxyPostProcessor, "CallingProxyPostProcessor");
        ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
        ParamChecker.assertNotNull(objectCollector, "objectCollector");
        ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
    }

    public void setCallingProxyPostProcessor(CallingProxyPostProcessor callingProxyPostProcessor) {
        this.callingProxyPostProcessor = callingProxyPostProcessor;
    }

    public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider) {
        this.propertyInfoProvider = propertyInfoProvider;
    }

    public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    public void setConversionHelper(IConversionHelper conversionHelper) {
        this.conversionHelper = conversionHelper;
    }

    protected int getBeanConfigurationAmount(BeanContextInit beanContextInit) {
        var beanConfigurations = beanContextInit.beanContextFactory.getBeanConfigurations();
        if (beanConfigurations == null) {
            return 0;
        }
        return beanConfigurations.size();
    }

    @Override
    public void initializeBeanContext(IServiceContextIntern beanContext) {
        var beanContextFactory = beanContext.getBeanContextFactory();
        if (beanContextFactory.getBeanConfigurations() == null) {
            return;
        }
        var objectToBeanConfigurationMap = new IdentityLinkedMap<Object, IBeanConfiguration>();
        var objectToHandledBeanConfigurationMap = new IdentityHashMap<Object, IBeanConfiguration>();
        var nameToBeanConfigurationMap = new LinkedHashMap<String, IBeanConfiguration>();
        var allLifeCycledBeansSet = new IdentityLinkedSet<>();
        var alreadyHandledConfigsSet = new IdentityHashSet<IBeanConfiguration>();
        var initializedOrdering = new ArrayList<>();
        var beanContextInit = new BeanContextInit();
        var oldBeanContextInit = currentBeanContextInitTL.get();
        try {
            currentBeanContextInitTL.set(beanContextInit);
            beanContextInit.beanContext = beanContext;
            beanContextInit.beanContextFactory = beanContextFactory;
            beanContextInit.objectToBeanConfigurationMap = objectToBeanConfigurationMap;
            beanContextInit.objectToHandledBeanConfigurationMap = objectToHandledBeanConfigurationMap;
            beanContextInit.allLifeCycledBeansSet = allLifeCycledBeansSet;
            beanContextInit.initializedOrdering = initializedOrdering;

            var contextProps = beanContextFactory.getProperties();
            beanContextInit.properties = contextProps;

            beanContextFactory.registerExternalBean("properties", contextProps).autowireable(IProperties.class, Properties.class);

            Object priorityBean;
            do {
                priorityBean = null;
                var highestPriority = 0;

                instantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, true);

                for (var entry : objectToBeanConfigurationMap) {
                    var bean = entry.getKey();

                    var priorityOfBean = getPriorityOfBean(bean.getClass());
                    if (priorityOfBean > highestPriority) {
                        highestPriority = priorityOfBean;
                        priorityBean = bean;
                        continue;
                    }
                }
                if (priorityBean != null) {
                    initializeBean(beanContextInit, priorityBean);
                }
            } while (priorityBean != null);

            while (true) {
                var beanConfigurationCountBefore = getBeanConfigurationAmount(beanContextInit);
                instantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, false);

                // Now load properties-service from the current context (it may be
                // another)
                beanContextInit.properties = beanContext.getService(Properties.class, false);

                resolveBeansInSequence(beanContextInit);
                int beanConfigurationCountAfter = getBeanConfigurationAmount(beanContextInit);
                if (beanConfigurationCountAfter == beanConfigurationCountBefore) {
                    break;
                }
            }
            checkIfAllBeanConfigsAreHandledCorrectly(beanContextInit, alreadyHandledConfigsSet);

            // Notify first all modules that this context is now ready
            for (int a = 0, size = initializedOrdering.size(); a < size; a++) {
                var bean = initializedOrdering.get(a);
                if (bean instanceof IStartingModule startingModule) {
                    startingModule.afterStarted(beanContext);
                }
            }
            // Then notify all link containers that this context is now ready for
            // linking
            for (int a = 0, size = initializedOrdering.size(); a < size; a++) {
                var bean = initializedOrdering.get(a);
                if (bean instanceof ILinkContainer linkContainer) {
                    try {
                        linkContainer.link();
                    } catch (Throwable e) {
                        throw maskBeanBasedException(e, beanContextInit, objectToHandledBeanConfigurationMap.get(linkContainer), null, linkContainer);
                    }
                }
            }
            var linkContainers = beanContext.getLinkContainers();
            if (linkContainers != null) {
                for (int a = 0, size = linkContainers.size(); a < size; a++) {
                    var linkContainer = linkContainers.get(a);
                    if (allLifeCycledBeansSet.contains(linkContainer)) {
                        // Nothing to do because this container has already been handled some lines before
                        continue;
                    }
                    linkContainer.link();
                }
            }
            beanContext.setRunning();
            // Then notify all "normal" beans that this context is now ready
            for (int a = 0, size = initializedOrdering.size(); a < size; a++) {
                var bean = initializedOrdering.get(a);
                if (bean instanceof IStartingBean startingBean) {
                    startingBean.afterStarted();
                }
            }
            publishMonitorableBeans(beanContextInit, initializedOrdering);
        } catch (Throwable e) {
            try {
                beanContext.dispose();
            } catch (Throwable ex) {
                throw RuntimeExceptionUtil.mask(ex, "Error occurred while disposing context while starting the context due to bean exception");
            }
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            currentBeanContextInitTL.set(oldBeanContextInit);
        }
    }

    protected StringBuilder convertBeanContextName(String beanContextName) {
        String[] split = beanContextName.split(Pattern.quote("/"));
        StringBuilder sb = new StringBuilder();
        sb.append("com.koch.ambeth:module=ioc,context=");
        for (int b = 0, sizeB = split.length; b < sizeB; b++) {
            if (b > 0) {
                sb.append(",context");
                sb.append(b);
                sb.append("=");
            }
            sb.append(split[b]);
        }
        sb.append(",name=");
        return sb;
    }

    protected ObjectName createMonitoringNameOfBean(StringBuilder beanContextName, IBeanConfiguration beanConfiguration) {
        int oldLength = beanContextName.length();
        beanContextName.append(beanConfiguration.getName());
        try {
            return new ObjectName(beanContextName.toString());
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            beanContextName.setLength(oldLength);
        }
    }

    protected void publishMonitorableBeans(BeanContextInit beanContextInit, List<Object> initializedOrdering) {
        boolean monitorBeansActive = Boolean.parseBoolean(beanContextInit.properties.getString(IocConfigurationConstants.MonitorBeansActive, "true"));
        if (!monitorBeansActive) {
            return;
        }
        var propertyInfoProvider = beanContextInit.beanContext.getService(IPropertyInfoProvider.class, false);
        if (propertyInfoProvider == null) {
            return;
        }
        var beanContext = beanContextInit.beanContext;
        var objectToHandledBeanConfigurationMap = beanContextInit.objectToHandledBeanConfigurationMap;
        var mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs == null) {
            // JMX not activated
            return;
        }
        var mBeans = new ArrayList<ObjectName>();
        var success = false;
        try {
            var beanContextName = convertBeanContextName(beanContext.getName());
            for (int a = 0, size = initializedOrdering.size(); a < size; a++) {
                var bean = initializedOrdering.get(a);
                var beanConfiguration = objectToHandledBeanConfigurationMap.get(bean);
                if (beanConfiguration == null || beanConfiguration.getName() == null) {
                    // beans without a name will not be browsable
                    continue;
                }
                Object mBean;
                if (!(bean instanceof DynamicMBean)) {
                    var bmSupport = new BeanMonitoringSupport(bean, beanContext);
                    if (bmSupport.getMBeanInfo().getAttributes().length == 0) {
                        continue;
                    }
                    mBean = bmSupport;
                } else {
                    mBean = bean;
                }
                try {
                    var name = createMonitoringNameOfBean(beanContextName, beanConfiguration);
                    mbs.registerMBean(mBean, name);
                    mBeans.add(name);
                } catch (Exception e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            }
            success = true;
        } finally {
            if (!success) {
                for (int a = mBeans.size(); a-- > 0; ) {
                    var name = mBeans.get(a);
                    try {
                        mbs.unregisterMBean(name);
                    } catch (Exception e) {
                        throw RuntimeExceptionUtil.mask(e);
                    }
                }
            }
        }
        beanContext.registerDisposeHook(currBeanContext -> {
            for (int a = mBeans.size(); a-- > 0; ) {
                var name = mBeans.get(a);
                mbs.unregisterMBean(name);
            }
        });
    }

    protected void resolveBeansInSequence(BeanContextInit beanContextInit) {
        var objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

        while (!objectToBeanConfigurationMap.isEmpty()) {
            for (var entry : objectToBeanConfigurationMap) {
                var bean = entry.getKey();

                initializeBean(beanContextInit, bean);
                break;
            }
        }
    }

    protected void checkIfAllBeanConfigsAreHandledCorrectly(BeanContextInit beanContextInit, Set<IBeanConfiguration> alreadyHandledConfigsSet) {
        var beanContextFactory = beanContextInit.beanContextFactory;
        // ServiceContext beanContext = beanContextInit.beanContext;
        var basicBeanConfigurations = beanContextFactory.getBeanConfigurations();
        if (basicBeanConfigurations != null) {
            for (int a = basicBeanConfigurations.size(); a-- > 0; ) {
                var beanConfiguration = basicBeanConfigurations.get(a);
                if (alreadyHandledConfigsSet.contains(beanConfiguration)) {
                    continue;
                }
                var hierarchy = new ArrayList<IBeanConfiguration>();
                var missingBeanName = fillParentHierarchyIfValid(beanContextInit, beanConfiguration, hierarchy);

                throw maskBeanBasedException("Parent bean definition '" + missingBeanName + "' not found", beanConfiguration, null);
            }
        }
    }

    protected void initializeAutowiring(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType, IPropertyInfo[] propertyInfos,
            Set<String> alreadySpecifiedPropertyNamesSet, Set<String> ignoredPropertyNamesSet) {
        var beanContext = beanContextInit.beanContext;
        var externalServiceContext = beanContext.getExternalServiceContext();
        var highPriorityBean = isHighPriorityBean(bean);
        for (var prop : propertyInfos) {
            var propertyName = prop.getName();
            if (alreadySpecifiedPropertyNamesSet.contains(propertyName)) {
                // Property already explicitly specified. No
                // auto-wiring necessary here
                continue;
            }
            if (prop.isAnnotationPresent(Self.class)) {
                // Self-annotated properties are not considered for auto-wiring
                continue;
            }
            if (ignoredPropertyNamesSet.contains(propertyName)) {
                // Property marked as ignored. No auto-wiring wanted here
                continue;
            }
            var propertyType = prop.getPropertyType();
            if (primitiveSet.contains(propertyType) || propertyType.isArray() && primitiveSet.contains(propertyType.getComponentType())) {
                continue;
            }
            var autowired = prop.getAnnotation(Autowired.class);
            if (autowired == null && externalServiceContext != null) {
                boolean hasBeenHandled =
                        externalServiceContext.initializeAutowiring(beanContextInit, beanConfiguration, beanContext, beanType, propertyInfos, alreadySpecifiedPropertyNamesSet, ignoredPropertyNamesSet,
                                this, highPriorityBean, prop);
                if (hasBeenHandled) {
                    continue;
                }
            }
            if (autowired == null && prop instanceof FieldPropertyInfo) {
                // Handle fields only if they are explicitly annotated
                continue;
            }
            var beanName = autowired != null ? autowired.value() : null;
            if (beanName != null && beanName.length() == 0) {
                beanName = null;
            }
            var fromContext = autowired != null ? autowired.fromContext() : null;
            if (fromContext != null && fromContext.length() == 0) {
                fromContext = null;
            }
            var refBean = resolveBean(fromContext, beanName, propertyType, highPriorityBean, beanContextInit);
            if (refBean == null) {
                if (autowired != null && !autowired.optional()) {
                    var sb = new StringBuilder();
                    sb.append("Could not resolve mandatory autowiring constraint on property '").append(prop.getName()).append("' of type '").append(propertyType.getName()).append('\'');
                    if (fromContext != null) {
                        sb.append(", lookup-context=CURRENT");
                    } else {
                        sb.append(", lookup-context=").append(fromContext);
                    }
                    if (beanName != null) {
                        sb.append(", lookup-bean-name=").append(beanName);
                    } else {
                        sb.append(", lookup-bean-type=").append(propertyType.getName());
                    }
                    throw maskBeanBasedException(sb.toString(), beanConfiguration, null);
                }
                continue;
            }
            prop.setValue(bean, refBean);
        }
    }

    public Object resolveBean(String fromContext, String beanName, Class<?> propertyType, boolean isHighPriorityBean, BeanContextInit beanContextInit) {
        IServiceContextIntern beanContext = beanContextInit.beanContext;
        var objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;
        // Module beans are only allowed to demand beans from the parent
        // context

        if (fromContext != null) {
            var refFromContext = (IServiceContextIntern) beanContext.getDirectBean(fromContext);
            if (refFromContext == null) {
                return null;
            }
            beanContext = refFromContext;
        }
        var refBean = beanName != null ? beanContext.getDirectBean(beanName) : beanContext.getDirectBean(propertyType);
        if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.containsKey(refBean)) {
            initializeBean(beanContextInit, refBean);
        }
        if (beanName != null) {
            return beanContext.getServiceIntern(beanName, propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
        }
        return beanContext.getServiceIntern(propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
    }

    @SneakyThrows
    protected void callInitializingCallbacks(BeanContextInit beanContextInit, Object bean, boolean joinLifecycle) {
        var beanContext = beanContextInit.beanContext;
        var initializedOrdering = beanContextInit.initializedOrdering;

        if (bean instanceof IInitializingBean) {
            ((IInitializingBean) bean).afterPropertiesSet();
        }
        if (bean instanceof IDisposableBean) {
            beanContextInit.toDestroyOnError.add((IDisposableBean) bean);
        }
        if (bean instanceof IPropertyLoadingBean) {
            ((IPropertyLoadingBean) bean).applyProperties(beanContextInit.properties);
        }
        if (bean instanceof IInitializingModule) {
            ((IInitializingModule) bean).afterPropertiesSet(beanContextInit.beanContextFactory);
            if (bean instanceof IDisposableBean && !(bean instanceof IInitializingBean)) {
                // it is a module (and only a module) so it has not been added yet
                beanContextInit.toDestroyOnError.add((IDisposableBean) bean);
            }
        }
        if (bean instanceof IBeanInstantiationProcessor) {
            beanContext.addInstantiationProcessor((IBeanInstantiationProcessor) bean);
        }
        if (bean instanceof IBeanPreProcessor) {
            beanContext.addPreProcessor((IBeanPreProcessor) bean);
        }
        if (bean instanceof IBeanPostProcessor) {
            beanContext.addPostProcessor((IBeanPostProcessor) bean);
        }
        if (bean instanceof ILinkContainer) {
            beanContext.addLinkContainer((ILinkContainer) bean);
            if (beanContext.isRunning()) {
                ((ILinkContainer) bean).link();
            }
        }
        if (joinLifecycle && bean instanceof IDisposableBean) {
            beanContext.registerDisposable((IDisposableBean) bean);
        }
        if (initializedOrdering != null) {
            initializedOrdering.add(bean);
        }
    }

    @Override
    public Object initializeBean(IServiceContextIntern beanContext, IBeanContextFactoryIntern beanContextFactory, IBeanConfiguration beanConfiguration, Object bean, List<IBeanConfiguration> beanConfHierarchy,
            boolean joinLifecycle) {
        var currentBeanContextInit = currentBeanContextInitTL.get();
        if (currentBeanContextInit == null) {
            currentBeanContextInit = new BeanContextInit();
            currentBeanContextInit.beanContext = beanContext;
            currentBeanContextInit.beanContextFactory = beanContextFactory;
            currentBeanContextInit.properties = beanContext.getService(Properties.class);
            currentBeanContextInit.objectToBeanConfigurationMap = new IdentityLinkedMap<>();
            currentBeanContextInit.objectToHandledBeanConfigurationMap = new IdentityHashMap<>();
        }
        initializeBean(currentBeanContextInit, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
        if (joinLifecycle && bean instanceof IStartingBean startingBean) {
            try {
                startingBean.afterStarted();
            } catch (Throwable e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
        return postProcessBean(currentBeanContextInit, beanConfiguration, beanConfiguration.getBeanType(), bean, beanConfHierarchy);
    }

    public void initializeBean(BeanContextInit beanContextInit, Object bean) {
        var beanConfiguration = beanContextInit.objectToBeanConfigurationMap.remove(bean);
        beanContextInit.objectToHandledBeanConfigurationMap.put(bean, beanConfiguration);
        var allLifeCycledBeansSet = beanContextInit.allLifeCycledBeansSet;

        var beanConfHierarchy = new ArrayList<IBeanConfiguration>(3);
        if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null) {
            throw maskBeanBasedException("Must never happen at this point", beanConfiguration, null);
        }
        allLifeCycledBeansSet.add(bean);

        initializeBean(beanContextInit, beanConfiguration, bean, beanConfHierarchy, true);
    }

    public void initializeBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, List<IBeanConfiguration> beanConfHierarchy, boolean joinLifecycle) {
        if (!(bean instanceof IInitializingModule) && !beanConfiguration.isWithLifecycle()) {
            if (bean instanceof IPropertyLoadingBean propertyLoadingBean) {
                propertyLoadingBean.applyProperties(beanContextInit.properties);
            }
            return;
        }
        var beanContext = beanContextInit.beanContext;
        var beanContextFactory = beanContextInit.beanContextFactory;
        var preProcessors = beanContext.getPreProcessors();

        var propertyConfigurations = new ArrayList<IPropertyConfiguration>();
        var alreadySpecifiedPropertyNamesSet = new HashSet<String>();

        try {
            var beanType = resolveTypeInHierarchy(beanConfHierarchy);
            resolveAllBeanConfInHierarchy(beanConfHierarchy, propertyConfigurations);
            var ignoredPropertyNames = resolveAllIgnoredPropertiesInHierarchy(beanConfHierarchy, beanType);

            var propertyInfos = propertyInfoProvider.getIocProperties(beanType);

            if (preProcessors != null) {
                var beanName = beanConfiguration.getName();
                var properties = beanContextInit.properties;
                for (int a = 0, size = preProcessors.size(); a < size; a++) {
                    var preProcessor = preProcessors.get(a);
                    preProcessor.preProcessProperties(beanContextFactory, beanContext, properties, beanName, bean, beanType, propertyConfigurations, ignoredPropertyNames, propertyInfos);
                }
            }
            initializeDefining(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, propertyConfigurations, alreadySpecifiedPropertyNamesSet);
            initializeAutowiring(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, alreadySpecifiedPropertyNamesSet, ignoredPropertyNames);
            callInitializingCallbacks(beanContextInit, bean, joinLifecycle);
        } catch (Throwable e) {
            throw maskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
        }
    }

    protected Throwable createBeanContextDeclarationExceptionIfPossible(Throwable e, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration) {
        if (e instanceof BeanContextDeclarationException || e instanceof BeanContextInitException) {
            return e;
        }
        StackTraceElement[] declarationStackTrace = null;
        if (propertyConfiguration != null) {
            declarationStackTrace = propertyConfiguration.getDeclarationStackTrace();
            if (declarationStackTrace == null) {
                declarationStackTrace = propertyConfiguration.getBeanConfiguration().getDeclarationStackTrace();
            }
        }
        if (declarationStackTrace == null && beanConfiguration != null) {
            declarationStackTrace = beanConfiguration.getDeclarationStackTrace();
        }
        if (declarationStackTrace == null) {
            return e;
        }
        if (e != null) {
            while (e instanceof MaskingRuntimeException && e.getMessage() == null) {
                e = e.getCause();
            }
            return new BeanContextDeclarationException(declarationStackTrace, e);
        }
        return new BeanContextDeclarationException(declarationStackTrace);
    }

    protected RuntimeException maskBeanBasedException(Throwable e, BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration, Object bean) {
        var tlObjectCollector = objectCollector.getCurrent();
        var sb = tlObjectCollector.create(StringBuilder.class);
        try {
            Class<?> beanType = null;
            if (bean != null) {
                beanType = bean.getClass();
            } else {
                var beanConfHierarchy = new ArrayList<IBeanConfiguration>();
                fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
                beanType = resolveTypeInHierarchy(beanConfHierarchy);
            }
            if (beanType != null && AbstractLinkContainer.class.isAssignableFrom(beanType)) {
                sb.append("Error occured while executing link operation");
            } else if (beanConfiguration.getName() == null) {
                sb.append("Error occured while handling anonymous bean of type ").append(beanType != null ? beanType.getName() : "<unknown>");
            } else {
                sb.append("Error occured while handling bean '").append(beanConfiguration.getName()).append("' of type ").append(beanType != null ? beanType.getName() : "<unknown>");
            }
            return maskBeanBasedException(sb, e, beanConfiguration, propertyConfiguration);
        } finally {
            tlObjectCollector.dispose(sb);
        }
    }

    public RuntimeException maskBeanBasedException(CharSequence message, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration) {
        return maskBeanBasedException(message, null, beanConfiguration, propertyConfiguration);
    }

    protected RuntimeException maskBeanBasedException(CharSequence message, Throwable e, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration) {
        e = createBeanContextDeclarationExceptionIfPossible(e, beanConfiguration, propertyConfiguration);

        var tlObjectCollector = objectCollector.getCurrent();
        var sb = tlObjectCollector.create(StringBuilder.class);
        try {
            sb.append(message);
            if (!(e instanceof BeanContextInitException)) {
                var beanContextInitException = new BeanContextInitException(sb.toString(), e);
                if (e != null) {
                    beanContextInitException.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
                }
                return beanContextInitException;
            }
            sb.insert(0, SystemUtil.lineSeparator());
            sb.insert(0, e.getMessage());
            var beanContextInitException = new BeanContextInitException(sb.toString(), e.getCause());
            beanContextInitException.setStackTrace(e.getStackTrace());
            return beanContextInitException;
        } finally {
            tlObjectCollector.dispose(sb);
        }
    }

    protected IPropertyInfo autoResolveProperty(Class<?> beanType, IPropertyConfiguration propertyConf, Set<String> alreadySpecifiedPropertyNamesSet) {
        return autoResolveAndSetPropertyIntern(null, beanType, null, propertyConf, null, null, alreadySpecifiedPropertyNamesSet);
    }

    protected void autoResolveAndSetProperties(Object bean, Class<?> beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf, String beanName, Object refBean,
            Set<String> alreadySpecifiedPropertyNamesSet) {
        autoResolveAndSetPropertyIntern(bean, beanType, properties, propertyConf, beanName, refBean, alreadySpecifiedPropertyNamesSet);
    }

    protected IPropertyInfo autoResolveAndSetPropertyIntern(Object bean, Class<?> beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf, String beanName, Object refBean,
            Set<String> alreadySpecifiedPropertyNamesSet) {
        var propertyName = propertyConf.getPropertyName();
        if (propertyName != null) {
            var property = propertyInfoProvider.getProperty(beanType, propertyName);
            if (property == null) {
                property = propertyInfoProvider.getProperty(beanType, StringConversionHelper.upperCaseFirst(objectCollector, propertyName));
                if (property == null) {
                    var fields = ReflectUtil.getDeclaredFieldInHierarchy(beanType, propertyName);
                    if (fields.length == 0) {
                        throw maskBeanBasedException("Bean property " + beanType.getName() + "." + propertyName + " not found", null, propertyConf);
                    }
                    return new FieldPropertyInfo(beanType, propertyInfoProvider.getPropertyNameFor(fields[0]), fields[0], null);
                }
            }
            return property;
        }
        var refBeanClass = refBean.getClass();
        var atLeastOnePropertyFound = false;
        // Autoresolve property name by type of the requested bean
        for (var property : properties) {
            if (!property.isWritable()) {
                continue;
            }
            if (alreadySpecifiedPropertyNamesSet.contains(property.getName())) {
                // Ignore all already handled properties for potential
                // autoresolving
                continue;
            }
            if (!property.getElementType().isAssignableFrom(refBeanClass)) {
                continue;
            }
            // At this point the property WILL match and we intend to see this
            // as property found
            // even if it has already been matched (and done) by another
            // propertyRef-definition before
            try {
                if (Optional.class.equals(property.getPropertyType())) {
                    property.setValue(bean, Optional.of(refBean));
                } else {
                    property.setValue(bean, refBean);
                }
                atLeastOnePropertyFound = true;
            } catch (Throwable e) {
                throw maskBeanBasedException("Propertyrefs did not work on type \"" + beanType + "\". Tried to set refbean \"" + refBean + "\" of type: \"" + refBeanClass + "\"", e, null,
                        propertyConf);
            }
            alreadySpecifiedPropertyNamesSet.add(property.getName());
        }
        if (!atLeastOnePropertyFound) {
            var currType = beanType;
            while (currType != Object.class) {
                var fields = ReflectUtil.getDeclaredFields(currType);
                for (var field : fields) {
                    var fieldName = field.getName();
                    if (alreadySpecifiedPropertyNamesSet.contains(fieldName)) {
                        // Ignore all already handled properties for potential
                        // autoresolving
                        continue;
                    }
                    if (!field.getType().isAssignableFrom(refBeanClass)) {
                        continue;
                    }
                    try {
                        field.set(bean, refBean);
                        atLeastOnePropertyFound = true;
                    } catch (Throwable e) {
                        throw maskBeanBasedException("Propertyrefs did not work on type \"" + beanType + "\". Tried to set refbean \"" + refBean + "\" of type: \"" + refBeanClass + "\"", e, null,
                                propertyConf);
                    }
                    alreadySpecifiedPropertyNamesSet.add(fieldName);
                }
                currType = currType.getSuperclass();
            }
        }
        if (!atLeastOnePropertyFound) {
            throw maskBeanBasedException(
                    "Impossible autoresolve property scenario: There is no property which accepts a bean of type " + refBeanClass.getName() + "' as represented by bean name '" + beanName + "'", null,
                    propertyConf);
        }
        return null;
    }

    protected void initializeDefining(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType, IPropertyInfo[] propertyInfos,
            List<IPropertyConfiguration> propertyConfigurations, Set<String> alreadySpecifiedPropertyNamesSet) {
        for (int a = propertyConfigurations.size(); a-- > 0; ) {
            var propertyConf = propertyConfigurations.get(a);

            try {
                var refBeanName = propertyConf.getBeanName();
                if (refBeanName == null) {
                    initializePrimitive(beanContextInit, bean, beanType, propertyConf, alreadySpecifiedPropertyNamesSet);
                    continue;
                }
                initializeRelation(beanContextInit, beanConfiguration, bean, beanType, propertyConf, propertyInfos, alreadySpecifiedPropertyNamesSet);
            } catch (Throwable e) {
                throw RuntimeExceptionUtil.mask(e, "Error occurred while setting property '" + propertyConf.getPropertyName() + "'");
            }
        }
    }

    protected void initializePrimitive(BeanContextInit beanContextInit, Object bean, Class<?> beanType, IPropertyConfiguration propertyConf, Set<String> alreadySpecifiedPropertyNamesSet) {
        var value = propertyConf.getValue();
        var properties = beanContextInit.properties;

        if (value instanceof String) {
            value = properties.resolvePropertyParts((String) value);

            if (value == null) {
                throw maskBeanBasedException("Environmental property '" + propertyConf.getValue() + "' could not be resolved while configuring bean property '" + propertyConf.getPropertyName() + "'",
                        null, propertyConf);
            }
        }
        var primitiveProperty = autoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

        var convertedValue = conversionHelper.convertValueToType(primitiveProperty.getPropertyType(), value);

        if (!alreadySpecifiedPropertyNamesSet.add(propertyConf.getPropertyName())) {
            log.debug("Property '" + propertyConf.getPropertyName() + "' already specified by higher priorized configuration. Ignoring setting property with value '" + convertedValue + "'");
            return;
        }
        primitiveProperty.setValue(bean, convertedValue);
    }

    protected boolean isHighPriorityBean(Object bean) {
        return isHighPriorityBean(bean.getClass());
    }

    protected boolean isHighPriorityBean(Class<?> beanType) {
        return getPriorityOfBean(beanType) != 0;
    }

    protected int getPriorityOfBean(Class<?> beanType) {
        if (IPropertyLoadingBean.class.isAssignableFrom(beanType)) {
            return 3;
        } else if (IBeanInstantiationProcessor.class.isAssignableFrom(beanType) || IBeanPreProcessor.class.isAssignableFrom(beanType) || IBeanPostProcessor.class.isAssignableFrom(beanType)) {
            return 2;
        } else if (IInitializingModule.class.isAssignableFrom(beanType)) {
            return 1;
        }
        return 0;
    }

    protected void initializeRelation(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType, IPropertyConfiguration propertyConf,
            IPropertyInfo[] propertyInfos, Set<String> alreadySpecifiedPropertyNamesSet) {
        var beanContext = beanContextInit.beanContext;
        var objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

        var refBeanName = propertyConf.getBeanName();

        Object refBean;
        if (propertyConf.getFromContext() != null) {
            var refBeanContext = beanContext.getDirectBean(propertyConf.getFromContext());
            if (refBeanContext == null) {
                throw maskBeanBasedException("IoC context bean '" + propertyConf.getFromContext() + "' not found to look for target bean", beanConfiguration, propertyConf);
            }
            beanContext = (ServiceContext) refBeanContext;
            refBean = beanContext.getServiceIntern(refBeanName, Object.class, SearchType.CASCADE);
        } else {
            // Module beans are only allowed to demand beans from the parent
            // context
            refBean = beanContext.getDirectBean(refBeanName);
            if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.containsKey(refBean)) {
                initializeBean(beanContextInit, refBean);
            }
            refBean = beanContext.getServiceIntern(refBeanName, Object.class, isHighPriorityBean(bean) ? SearchType.PARENT : SearchType.CASCADE);
            if (refBean != null) {
                IBeanConfiguration refBeanConfiguration = beanContextInit.objectToBeanConfigurationMap.get(refBean);
                if (refBeanConfiguration != null) {
                    // Object is not yet initialized. We try to do this before we use it
                    initializeBean(beanContextInit, refBean);
                }
            }
        }
        if (refBean == null) {
            if (propertyConf.isOptional()) {
                return;
            }
            String message;
            if (propertyConf.getPropertyName() != null) {
                message = "Bean '" + refBeanName + "' not found to set bean property '" + propertyConf.getPropertyName() + "'";
            } else {
                message = "Bean '" + refBeanName + "' not found to look for autoresolve property";
            }
            throw maskBeanBasedException(message, beanConfiguration, propertyConf);
        }
        if (propertyConf.getPropertyName() == null) {
            autoResolveAndSetProperties(bean, beanType, propertyInfos, propertyConf, refBeanName, refBean, alreadySpecifiedPropertyNamesSet);
            return;
        }
        var refProperty = autoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

        if (!alreadySpecifiedPropertyNamesSet.add(refProperty.getName())) {
            log.debug("Property '" + refProperty.getName() + "' already specified by higher priorized configuration. Ignoring setting property with ref to bean '" + refBeanName + "'");
            return;
        }
        if (!refProperty.getPropertyType().isAssignableFrom(refBean.getClass())) {
            throw maskBeanBasedException(
                    "Impossible property scenario: Property '" + propertyConf.getPropertyName() + "' does not accept a bean of type '" + refBean.getClass().getName() + "' as represented " +
                            "by bean name '" + refBeanName + "'", beanConfiguration, propertyConf);
        }
        refProperty.setValue(bean, refBean);
    }

    protected void resolveAllBeanConfInHierarchy(List<IBeanConfiguration> beanConfigurations, List<IPropertyConfiguration> propertyConfs) {
        for (int a = 0, size = beanConfigurations.size(); a < size; a++) {
            var beanConfiguration = beanConfigurations.get(a);
            var propertyConfigurations = beanConfiguration.getPropertyConfigurations();
            if (propertyConfigurations != null) {
                propertyConfs.addAll(propertyConfigurations);
            }
        }
    }

    protected void instantiateBeans(BeanContextInit beanContextInit, IMap<String, IBeanConfiguration> nameToBeanConfigurationMap, Set<IBeanConfiguration> alreadyHandledConfigsSet,
            boolean highPriorityOnly) {
        var beanContextFactory = beanContextInit.beanContextFactory;
        var beanConfigurations = beanContextFactory.getBeanConfigurations();
        if (beanConfigurations == null || beanConfigurations.isEmpty()) {
            return;
        }
        var beanContext = beanContextInit.beanContext;
        var objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

        var orderToHighBeanConfigurations = new HashMap<Integer, OrderState>();
        var orderToLowBeanConfigurations = new HashMap<Integer, OrderState>();

        sortBeanConfigurations(beanContextInit, beanConfigurations, alreadyHandledConfigsSet, orderToHighBeanConfigurations, orderToLowBeanConfigurations, highPriorityOnly);

        var beanConfHierarchy = new ArrayList<IBeanConfiguration>();

        boolean atLeastOneHandled = true;
        while (atLeastOneHandled) {
            atLeastOneHandled = false;

            while (true) {
                var beanConfigState = resolveNextPrecedenceBean(beanContextInit, orderToHighBeanConfigurations, orderToLowBeanConfigurations, highPriorityOnly);
                if (beanConfigState == null) {
                    break;
                }
                var beanConfiguration = beanConfigState.getBeanConfiguration();
                var beanType = beanConfigState.getBeanType();

                Object bean = null;
                try {
                    if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null) {
                        throw new IllegalStateException("Bean configuration must be valid at this point");
                    }
                    bean = instantiateBean(beanContext, beanContextFactory, beanConfiguration, beanType, beanConfHierarchy);

                    alreadyHandledConfigsSet.add(beanConfiguration);
                    atLeastOneHandled = true;

                    if (!objectToBeanConfigurationMap.putIfNotExists(bean, beanConfiguration)) {
                        throw maskBeanBasedException("Bean instance " + bean + " registered twice.", beanConfiguration, null);
                    }
                    bean = postProcessBean(beanContextInit, beanConfiguration, beanType, bean, beanConfHierarchy);
                    beanConfHierarchy.clear();

                    publishNamesAndAliasesAndTypes(beanContextInit, beanConfiguration, bean);

                    var currBeanConf = beanConfiguration;
                    while (currBeanConf.getParentName() != null) {
                        var parentBeanConf = beanContext.getBeanConfiguration(beanContextFactory, currBeanConf.getParentName());

                        if (parentBeanConf == null) {
                            throw maskBeanBasedException("Parent bean with name '" + currBeanConf.getParentName() + "' not found", beanConfiguration, null);
                        }
                        if (!parentBeanConf.isAbstract()) {
                            // The parent bean definition is a valid bean by
                            // itself. So the parent hierarchy will not be
                            // handled here
                            break;
                        }
                        publishNamesAndAliasesAndTypes(beanContextInit, parentBeanConf, bean);
                        currBeanConf = parentBeanConf;
                    }
                } catch (Throwable e) {
                    throw maskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
                }
            }
        }
    }

    @Override
    public Object instantiateBean(IServiceContextIntern beanContext, IBeanContextFactoryIntern beanContextFactory, IBeanConfiguration beanConfiguration, Class<?> beanType,
            List<IBeanConfiguration> beanConfHierarchy) {
        var beanInstantiationProcessors = beanContext.getInstantiationProcessors();
        if (beanInstantiationProcessors != null) {
            for (int a = 0, size = beanInstantiationProcessors.size(); a < size; a++) {
                var beanInstantiationProcessor = beanInstantiationProcessors.get(a);
                var bean = beanInstantiationProcessor.instantiateBean(beanContextFactory, beanContext, beanConfiguration, beanType, beanConfHierarchy);
                if (bean != null) {
                    return bean;
                }
            }
        }
        if (beanConfiguration instanceof BeanConfiguration) {
            return beanConfiguration.getInstance(beanType);
        } else if (beanConfiguration instanceof BeanInstanceConfiguration) {
            return beanConfiguration.getInstance();
        }
        throw maskBeanBasedException("Instance of '" + beanConfiguration.getClass() + "' not supported here", beanConfiguration, null);
    }

    protected Object postProcessBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Class<?> beanType, Object bean, List<IBeanConfiguration> beanConfHierarchy) {
        var beanContext = beanContextInit.beanContext;
        var beanContextFactory = beanContextInit.beanContextFactory;
        var postProcessors = beanContext.getPostProcessors();
        if (postProcessors == null) {
            return bean;
        }
        var allAutowireableTypes = new HashSet<Class<?>>();
        resolveAllAutowireableInterfacesInHierarchy(beanConfHierarchy, allAutowireableTypes);

        var allInterfaces = bean.getClass().getInterfaces();
        for (int b = allInterfaces.length; b-- > 0; ) {
            var implementingInterface = allInterfaces[b];
            allAutowireableTypes.add(implementingInterface);
        }
        // Do not manipulate the bean variable until all
        // postprocessors have been called without failure
        var currBean = bean;

        for (int b = 0, sizeB = postProcessors.size(); b < sizeB; b++) {
            var postProcessor = postProcessors.get(b);
            try {
                currBean = postProcessor.postProcessBean(beanContextFactory, beanContext, beanConfiguration, beanType, currBean, allAutowireableTypes);
            } catch (Throwable e) {
                throw maskBeanBasedException("Error occured while post-processing with '" + postProcessor + "'", e, beanConfiguration, null);
            }
            if (currBean == null) {
                throw new IllegalStateException("Bean post processor " + postProcessor.getClass().getName() + " did not return the bean");
            }
        }
        callingProxyPostProcessor.beanPostProcessed(beanContextFactory, beanContext, beanConfiguration, beanType, currBean, bean);
        return currBean;
    }

    protected void sortBeanConfigurations(BeanContextInit beanContextInit, List<IBeanConfiguration> beanConfigurations, Set<IBeanConfiguration> alreadyHandledConfigsSet,
            Map<Integer, OrderState> orderToHighBeanConfigurations, Map<Integer, OrderState> orderToLowBeanConfigurations, boolean highPriorityOnly) {
        var beanConfHierarchy = new ArrayList<IBeanConfiguration>();
        for (int a = 0, size = beanConfigurations.size(); a < size; a++) {
            var beanConfiguration = beanConfigurations.get(a);
            if (alreadyHandledConfigsSet.contains(beanConfiguration)) {
                // Already handled so we do not bother anymore
                continue;
            }
            if (beanConfiguration.isAbstract()) {
                // Abstract bean configurations will not be instantiated -
                // they are templates for other beans
                alreadyHandledConfigsSet.add(beanConfiguration);
                continue;
            }
            beanConfHierarchy.clear();
            if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null) {
                // Something in the hierarchy is currently not valid
                // Maybe with another module in this context the parent bean
                // definitions can be resolved later
                continue;
            }
            var currentBeanType = resolveTypeInHierarchy(beanConfHierarchy);
            var highPriority = isHighPriorityBean(currentBeanType);
            if (highPriorityOnly && !highPriority) {
                continue;
            }
            var orderToBeanConfigurations = highPriority ? orderToHighBeanConfigurations : orderToLowBeanConfigurations;

            var currentPrecedenceType = beanConfiguration.getPrecedence();
            var order = precedenceOrder.get(currentPrecedenceType);

            var list = orderToBeanConfigurations.get(order);
            if (list == null) {
                list = new OrderState();
                orderToBeanConfigurations.put(order, list);
            }
            list.add(new BeanConfigState(beanConfiguration, currentBeanType));
        }
    }

    protected BeanConfigState resolveNextPrecedenceBean(BeanContextInit beanContextInit, Map<Integer, OrderState> orderToHighBeanConfigurations, Map<Integer, OrderState> orderToLowBeanConfigurations,
            boolean highPriorityOnly) {
        var orders = new ArrayList<Integer>(orderToHighBeanConfigurations.keySet());
        Collections.sort(orders);
        for (int a = 0, size = orders.size(); a < size; a++) {
            var list = orderToHighBeanConfigurations.get(orders.get(a));
            var beanConfigState = list.consumeBeanConfigState();
            if (beanConfigState != null) {
                return beanConfigState;
            }
        }
        orders = new ArrayList<>(orderToLowBeanConfigurations.keySet());
        Collections.sort(orders);
        for (int a = 0, size = orders.size(); a < size; a++) {
            var list = orderToLowBeanConfigurations.get(orders.get(a));
            var beanConfigState = list.consumeBeanConfigState();
            if (beanConfigState != null) {
                return beanConfigState;
            }
        }
        return null;
    }

    @Override
    public List<IBeanConfiguration> fillParentHierarchyIfValid(IServiceContextIntern beanContext, IBeanContextFactoryIntern beanContextFactory, IBeanConfiguration beanConfiguration) {
        var beanContextInit = new BeanContextInit();
        beanContextInit.beanContext = beanContext;
        beanContextInit.beanContextFactory = beanContextFactory;
        beanContextInit.properties = beanContext.getService(Properties.class);

        var beanConfHierarchy = new ArrayList<IBeanConfiguration>();
        var missingBeanName = fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
        if (missingBeanName == null) {
            return beanConfHierarchy;
        }
        throw maskBeanBasedException("Illegal bean hierarchy: Bean '" + missingBeanName + "' not found", beanConfiguration, null);
    }

    public String fillParentHierarchyIfValid(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, List<IBeanConfiguration> targetBeanList) {
        targetBeanList.add(beanConfiguration);
        var currBeanConf = beanConfiguration;
        while (currBeanConf.getParentName() != null) {
            var parentBeanConf = beanContextInit.beanContext.getBeanConfiguration(beanContextInit.beanContextFactory, currBeanConf.getParentName());

            if (parentBeanConf == null) {
                targetBeanList.clear();
                return currBeanConf.getParentName();
            }
            targetBeanList.add(parentBeanConf);

            currBeanConf = parentBeanConf;
        }
        return null;
    }

    protected void publishNamesAndAliasesAndTypes(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean) {
        var beanContext = beanContextInit.beanContext;
        var beanContextFactory = beanContextInit.beanContextFactory;

        var beanName = beanConfiguration.getName();
        if (beanName != null && beanName.length() > 0) {
            if (!beanConfiguration.isAbstract()) {
                beanContext.addNamedBean(beanName, bean);
            }
            var beanNameToAliasesMap = beanContextFactory.getBeanNameToAliasesMap();
            if (beanNameToAliasesMap != null) {
                var aliasList = beanNameToAliasesMap.get(beanName);
                if (aliasList != null) {
                    for (int a = aliasList.size(); a-- > 0; ) {
                        var aliasName = aliasList.get(a);
                        beanContext.addNamedBean(aliasName, bean);
                    }
                }
            }
        }
        var autowireableTypes = beanConfiguration.getAutowireableTypes();
        if (autowireableTypes != null) {
            for (int autowireableIndex = autowireableTypes.size(); autowireableIndex-- > 0; ) {
                var autowireableType = autowireableTypes.get(autowireableIndex);
                beanContext.addAutowiredBean(autowireableType, bean);
            }
        }
    }

    @Override
    public Class<?> resolveTypeInHierarchy(List<IBeanConfiguration> beanConfigurations) {
        for (int a = 0, size = beanConfigurations.size(); a < size; a++) {
            var beanConfiguration = beanConfigurations.get(a);
            var type = beanConfiguration.getBeanType();
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    protected ISet<String> resolveAllIgnoredPropertiesInHierarchy(List<IBeanConfiguration> beanConfHierarchy, Class<?> beanType) {
        ISet<String> ignoredProperties = null;
        var propertyMap = propertyInfoProvider.getIocPropertyMap(beanType);
        for (int a = 0, size = beanConfHierarchy.size(); a < size; a++) {
            var beanConfiguration = beanConfHierarchy.get(a);
            var ignoredPropertyNames = beanConfiguration.getIgnoredPropertyNames();
            if (ignoredPropertyNames == null) {
                continue;
            }
            for (int b = ignoredPropertyNames.size(); b-- > 0; ) {
                var ignoredPropertyName = ignoredPropertyNames.get(b);

                if (!propertyMap.containsKey(ignoredPropertyName)) {
                    var uppercaseFirst = StringConversionHelper.upperCaseFirst(objectCollector, ignoredPropertyName);
                    if (!propertyMap.containsKey(uppercaseFirst)) {
                        throw maskBeanBasedException(
                                "Property '" + ignoredPropertyName + "' not found to ignore. This is only a check for consistency. However the following list of properties has been found: " +
                                        propertyMap.keySet(), beanConfiguration, null);
                    }
                    ignoredPropertyName = uppercaseFirst;
                }
                if (ignoredProperties == null) {
                    ignoredProperties = new HashSet<>();
                }
                ignoredProperties.add(ignoredPropertyName);
            }
        }
        if (ignoredProperties == null) {
            ignoredProperties = EmptySet.<String>emptySet();
        }
        return ignoredProperties;
    }

    protected void resolveAllAutowireableInterfacesInHierarchy(List<IBeanConfiguration> beanConfHierarchy, Set<Class<?>> autowireableInterfaces) {
        for (int a = 0, size = beanConfHierarchy.size(); a < size; a++) {
            var beanConfiguration = beanConfHierarchy.get(a);
            var autowireableTypes = beanConfiguration.getAutowireableTypes();
            if (autowireableTypes != null) {
                autowireableInterfaces.addAll(autowireableTypes);
            }
        }
    }
}
