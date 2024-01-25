package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.link.ILinkController;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededConfiguration;
import com.koch.ambeth.ioc.link.LinkConfiguration;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SpringBeanContextFactory implements IBeanContextFactory {

    @SneakyThrows
    public static Runnable processModuleInSpring(ConfigurableListableBeanFactory beanFactory, IInitializingModule module) {
        var ambethFactory = new SpringBeanContextFactory(beanFactory);
        ambethFactory.injectProperties(null, module, null);
        beanFactory.autowireBeanProperties(module, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        module.afterPropertiesSet(ambethFactory);
        return () -> ambethFactory.finalizePendingConfigurations();
    }

    final ConfigurableListableBeanFactory beanFactory;

    final ILinkController linkController;

    final IProxyFactory proxyFactory;

    final IProperties props;

    final List<IBeanConfiguration> pendingConfigurations = new ArrayList<>();

    @Getter(lazy = true)
    private final SpringBeanHelper springBeanHelper = createSpringBeanHelper();

    @Getter(lazy = true)
    private final SpringLinkManager linkManager = createSpringLinkManager();

    public SpringBeanContextFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory must be valid");
        linkController = beanFactory.getBean("linkController", ILinkController.class);
        proxyFactory = beanFactory.getBean("proxyFactory", IProxyFactory.class);
        props = beanFactory.getBean("properties", IProperties.class);
    }

    private SpringBeanHelper createSpringBeanHelper() {
        return AmbethSpringUtil.withContext((BeanDefinitionRegistry) beanFactory);
    }

    private SpringLinkManager createSpringLinkManager() {
        return AmbethSpringUtil.linkManager(beanFactory);
    }

    public void finalizePendingConfigurations() {
        for (var pendingConfiguration : pendingConfigurations) {
            var beanDef = (GenericBeanDefinition) beanFactory.getBeanDefinition(pendingConfiguration.getName());

            var propertyConfigs = pendingConfiguration.getPropertyConfigurations();
            if (propertyConfigs != null) {
                for (var propertyConfig : propertyConfigs) {
                    var propertyName = propertyConfig.getPropertyName();
                    var beanName = propertyConfig.getBeanName();
                    Object value;
                    if (beanName != null) {
                        value = beanFactory.getBean(beanName);
                    } else {
                        value = propertyConfig.getValue();
                    }
                    if (propertyName == null) {
                        var valueType = value.getClass();
                        if (value instanceof IBeanConfiguration valueConf) {
                            valueType = valueConf.getBeanType();
                        }
                        // resolve applicable property names dynamically
                        var propertyInfos = beanFactory.getBean(IPropertyInfoProvider.class).getProperties(beanDef.getBeanClass());
                        var matchesFound = 0;
                        for (var propertyInfo : propertyInfos) {
                            if (!Modifier.isPublic(propertyInfo.getModifiers()) && propertyInfo.getAnnotation(com.koch.ambeth.ioc.annotation.Autowired.class) == null) {
                                continue;
                            }
                            if (propertyInfo.getElementType().isAssignableFrom(valueType)) {
                                if (value instanceof IBeanConfiguration valueConf) {
                                    value = valueConf.getInstance(propertyInfo.getElementType());
                                }
                                if (Optional.class.equals(propertyInfo.getPropertyType())) {
                                    value = Optional.ofNullable(value);
                                }
                                beanDef.getPropertyValues().add(propertyInfo.getName(), value);
                                matchesFound++;
                            }
                        }
                        if (matchesFound == 0) {
                            throw new IllegalStateException("No injection point found on " + beanDef + " in order to inject " + value);
                        }
                    } else {
                        beanDef.getPropertyValues().add(propertyName, value);
                    }
                }
            }
        }
    }

    public void injectProperties(String beanName, Object bean, IBeanConfiguration beanConfiguration) {
        var propertiesPreProcessor = beanFactory.getBean(SpringPropertiesPreProcessor.class);
        var ignoredPropertyNames = beanConfiguration != null ? new HashSet<>(beanConfiguration.getIgnoredPropertyNames()) : Set.<String>of();
        var propertyConfigurations = beanConfiguration != null ? beanConfiguration.getPropertyConfigurations() : List.<IPropertyConfiguration>of();
        propertiesPreProcessor.preProcessProperties(this, null, props, beanName, bean, bean.getClass(), propertyConfigurations, ignoredPropertyNames, null);
    }

    @Override
    public IBeanConfiguration registerWithLifecycle(Object object) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(object.getClass(), (name, beanDef) -> {
            beanDef.setInstanceSupplier(() -> object);
        });
        var beanConf = new BeanConfiguration(object.getClass(), beanReference.getBeanName(), proxyFactory, props, object);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public void registerDisposable(IDisposable disposable) {
        getSpringBeanHelper().createBeanDefinition(ContextCloseListener.class, (beanName, bean) -> {
            bean.getPropertyValues().add("disposable", disposable);
        });
    }

    @Override
    public void registerDisposable(IDisposableBean disposableBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration registerExternalBean(Object externalBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration registerAnonymousBean(Class<?> beanType) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(beanType);
        var beanConf = new BeanConfiguration(beanType, beanReference.getBeanName(), proxyFactory, props);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public IBeanConfiguration registerBean(Class<?> beanType) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(beanType, (beanName, beanDef) -> {
        });
        var beanConf = new BeanConfiguration(beanType, beanReference.getBeanName(), proxyFactory, props);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public void registerAlias(String aliasBeanName, String beanNameToCreateAliasFor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration registerBean(String beanName, String parentBeanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration registerBean(String beanName, Class<?> beanType) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(beanName, beanType);
        var beanConf = new BeanConfiguration(beanType, beanReference.getBeanName(), proxyFactory, props);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public <I, T extends I> IBeanConfiguration registerAutowireableBean(Class<I> interfaceType, Class<T> beanType) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(beanType);
        var beanConf = new BeanConfiguration(beanType, beanReference.getBeanName(), proxyFactory, props);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public IBeanConfiguration registerWithLifecycle(String beanName, Object object) {
        var beanReference = getSpringBeanHelper().createBeanDefinition(beanName, object.getClass(), (name, beanDef) -> {
            beanDef.setInstanceSupplier(() -> object);
        });
        var beanConf = new BeanConfiguration(object.getClass(), beanReference.getBeanName(), proxyFactory, props, object);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    @Override
    public IBeanConfiguration registerExternalBean(String beanName, Object externalBean) {
        var beanType = externalBean.getClass();
        beanFactory.registerSingleton(beanName, externalBean);

        var beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(beanType);
        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanName, beanDef);
        beanDef.setInstanceSupplier(() -> externalBean);

        var beanConf = instanceToConfiguration(beanName, externalBean, false);
        pendingConfigurations.add(beanConf);
        return beanConf;
    }

    protected IBeanConfiguration instanceToConfiguration(String beanName, Object beanInstance, boolean withLifecycle) {
        return new IBeanConfiguration() {
            @Override
            public StackTraceElement[] getDeclarationStackTrace() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getInstance() {
                return beanInstance;
            }

            @Override
            public Object getInstance(Class<?> instanceType) {
                return beanInstance;
            }

            @Override
            public String getName() {
                return beanName;
            }

            @Override
            public String getParentName() {
                return null;
            }

            @Override
            public Class<?> getBeanType() {
                return beanInstance.getClass();
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public boolean isWithLifecycle() {
                return withLifecycle;
            }

            @Override
            public PrecedenceType getPrecedence() {
                return PrecedenceType.DEFAULT;
            }

            @Override
            public boolean isOverridesExisting() {
                return false;
            }

            @Override
            public List<String> getIgnoredPropertyNames() {
                return List.of();
            }

            @Override
            public List<Class<?>> getAutowireableTypes() {
                return List.of();
            }

            @Override
            public List<IPropertyConfiguration> getPropertyConfigurations() {
                return List.of();
            }

            @Override
            public IBeanConfiguration precedence(PrecedenceType precedenceType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration template() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration parent(String parentBeanTemplateName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration overridesExisting() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration autowireable(Class<?> typeToPublish) {
                // intended blank
                return this;
            }

            @Override
            public IBeanConfiguration autowireable(Class<?>... typesToPublish) {
                // intended blank
                return this;
            }

            @Override
            public IBeanConfiguration propertyRef(String propertyName, String beanName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRef(String propertyName, IBeanConfiguration bean) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRefFromContext(String propertyName, String fromContext, String beanName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRefs(String beanName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRefs(String... beanNames) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRef(IBeanConfiguration bean) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyRefs(IBeanConfiguration... beans) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration propertyValue(String propertyName, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration ignoreProperties(String propertyName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IBeanConfiguration ignoreProperties(String... propertyNames) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName) {
        var linkConfiguration = linkController.createLinkConfiguration(listenerBeanName, (String) null);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    protected void addLinkConfiguration(LinkConfiguration<?> linkConfiguration) {
        var beanDef = getSpringBeanHelper().createBeanDefinition(linkConfiguration.getBeanType());
        linkConfiguration.setBeanName(beanDef.getBeanName());
        pendingConfigurations.add(linkConfiguration);
    }

    @Override
    public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName, String methodName) {
        var linkConfiguration = linkController.createLinkConfiguration(listenerBeanName, methodName);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    @Override
    public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean) {
        var linkConfiguration = linkController.createLinkConfiguration(listenerBean, null);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    @Override
    public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean, String methodName) {
        var linkConfiguration = linkController.createLinkConfiguration(listenerBean, methodName);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    @Override
    public <D> ILinkRegistryNeededConfiguration<D> link(D listener) {
        var linkConfiguration = linkController.createLinkConfiguration(listener, null);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    @Override
    public ILinkRegistryNeededConfiguration<?> link(Object listener, String methodName) {
        var linkConfiguration = linkController.createLinkConfiguration(listener, methodName);
        addLinkConfiguration(linkConfiguration);
        return linkConfiguration;
    }

    @Override
    public void link(String listenerBeanName, Class<?> autowiredRegistryClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments) {
        throw new UnsupportedOperationException();
    }
}
