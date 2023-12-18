package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.accessor.AccessorTypeProvider;
import com.koch.ambeth.ioc.bytecode.ClassCache;
import com.koch.ambeth.ioc.bytecode.SimpleClassLoaderProvider;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.extendable.ExtendableRegistry;
import com.koch.ambeth.ioc.garbageproxy.GarbageProxyFactory;
import com.koch.ambeth.ioc.link.LinkController;
import com.koch.ambeth.ioc.link.SpringBeanLookup;
import com.koch.ambeth.ioc.log.LoggerHistory;
import com.koch.ambeth.ioc.proxy.CallingProxyPostProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import com.koch.ambeth.ioc.threadlocal.ThreadLocalCleanupController;
import com.koch.ambeth.ioc.typeinfo.PropertyInfoProvider;
import com.koch.ambeth.ioc.util.ConversionHelper;
import com.koch.ambeth.ioc.util.DelegatingConversionHelper;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.util.DelegateFactory;
import com.koch.ambeth.util.InterningFeature;
import com.koch.ambeth.util.StringBuilderCollectableController;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;
import com.koch.ambeth.util.objectcollector.NoOpObjectCollector;
import com.koch.ambeth.util.objectcollector.ObjectCollector;
import com.koch.ambeth.util.objectcollector.ThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.ProxyFactory;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.env.Environment;

import java.util.function.Function;

@Configuration
public class AmbethBootstrapSpringConfig implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private static final ThreadLocal<Function<DefaultListableBeanFactory, IProperties>> factoryPostProcessorTL = new NamedThreadLocal<>("AmbethBootstrapSpringConfig.factoryPostProcessorTL");

    public static IStateRollback pushBeanFactoryPostProcessor(Function<DefaultListableBeanFactory, IProperties> factoryPostProcessor) {
        var oldPostProcessor = factoryPostProcessorTL.get();
        factoryPostProcessorTL.set(factoryPostProcessor);
        if (oldPostProcessor == null) {
            return () -> factoryPostProcessorTL.remove();
        }
        return () -> factoryPostProcessorTL.set(oldPostProcessor);
    }

    @Setter
    @Value("${" + IocConfigurationConstants.UseObjectCollector + ":true}")
    boolean useObjectCollector = true;

    @Setter
    Environment environment;

    IProperties props;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var factoryPostProcessor = factoryPostProcessorTL.get();
        if (factoryPostProcessor != null) {
            props = factoryPostProcessor.apply((DefaultListableBeanFactory) beanFactory);
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        var springHelper = AmbethSpringUtil.withContext(registry);

        BeanReference threadLocalObjectCollector;
        if (useObjectCollector) {
            threadLocalObjectCollector = springHelper.createBeanDefinition(ThreadLocalObjectCollector.BEAN_NAME, ThreadLocalObjectCollector.class, (beanName, bean) -> {
                //bean.setPrimary(true);
            });
        } else {
            threadLocalObjectCollector = springHelper.createBeanDefinition(ThreadLocalObjectCollector.BEAN_NAME, NoOpObjectCollector.class);
        }
        var propertyInfoProvider = springHelper.createBeanDefinition(PropertyInfoProvider.class, "objectCollector", threadLocalObjectCollector);

        var loggerInstancePreProcessor = springHelper.createBeanDefinition(SpringLoggerInstancePreProcessor.class, "objectCollector", threadLocalObjectCollector);

        var callingProxyPostProcessor = springHelper.createBeanDefinition(CallingProxyPostProcessor.class, "propertyInfoProvider", propertyInfoProvider);

        var loggerHistory = springHelper.createBeanDefinition(LoggerHistory.class);

        var accessorTypeProvider = springHelper.createBeanDefinition(AccessorTypeProvider.class);

        var garbageProxyFactory = springHelper.createBeanDefinition(GarbageProxyFactory.class, "accessorTypeProvider", accessorTypeProvider);

        var extendableRegistry = springHelper.createBeanDefinition(ExtendableRegistry.class, "objectCollector", threadLocalObjectCollector);

        var interningFeature = springHelper.createBeanDefinition(InterningFeature.class);

        var explicitClassLoader = environment.getProperty(IocConfigurationConstants.ExplicitClassLoader, ClassLoader.class);
        var classLoaderProvider = springHelper.createBeanDefinition(SimpleClassLoaderProvider.class, "classLoader", explicitClassLoader);

        var classCache = springHelper.createBeanDefinition(ClassCache.class, (beanName, bean) -> {
            bean.getPropertyValues().add("classLoaderProvider", classLoaderProvider);
            bean.getPropertyValues().add("interningFeature", interningFeature);
        });

        var conversionHelper = springHelper.createBeanDefinition(ConversionHelper.class, "classCache", classCache);

        var delegatingConversionHelper = springHelper.createBeanDefinition(DelegatingConversionHelper.class, "defaultConversionHelper", conversionHelper);

        var proxyFactory = springHelper.createBeanDefinition(ProxyFactory.class, "classLoaderProvider", classLoaderProvider);

        var linkController = springHelper.createBeanDefinition(LinkController.class, (beanName, bean) -> {
            bean.getPropertyValues().add("extendableRegistry", extendableRegistry);
            bean.getPropertyValues().add("props", new RuntimeBeanReference("properties"));
            bean.getPropertyValues().add("proxyFactory", proxyFactory);
        });

        var immutableTypeSet = springHelper.createBeanDefinition(ImmutableTypeSet.class);

        var delegateFactory = springHelper.createBeanDefinition(DelegateFactory.class);

        var springLinkManager = springHelper.createBeanDefinition(SpringLinkManager.class);

        var beanLookup = springHelper.createBeanDefinition(SpringBeanLookup.class);

        var threadLocalCleanupPreProcessor = springHelper.createBeanDefinition(SpringAutoLinkPreProcessor.class, (beanName, bean) -> {
            bean.getPropertyValues().add("loggerCache", loggerInstancePreProcessor);
            bean.getPropertyValues().add("extendableRegistry", extendableRegistry);
            bean.getPropertyValues().add("extendableType", IThreadLocalCleanupBeanExtendable.class);
            bean.getPropertyValues().add("props", new RuntimeBeanReference("properties"));
            bean.getPropertyValues().add("proxyFactory", proxyFactory);
            bean.getPropertyValues().add("beanLookup", beanLookup);
            bean.getPropertyValues().add("springLinkManager", springLinkManager);
        });

        if (useObjectCollector) {
            var threadLocalCleanupController = springHelper.createBeanDefinition(ThreadLocalCleanupController.class, "objectCollector", threadLocalObjectCollector);

            var objectCollector = springHelper.createBeanDefinition(ObjectCollector.BEAN_NAME, ObjectCollector.class, (beanName, bean) -> {
                bean.setAutowireCandidate(false);
                bean.getPropertyValues().add("threadLocalObjectCollector", threadLocalObjectCollector);
            });

            var linkContainer = springHelper.createBeanDefinition(SpringLinkContainer.class, (beanName, bean) -> {
                bean.getPropertyValues().add("extendableRegistry", extendableRegistry);
                bean.getPropertyValues().add("proxyFactory", proxyFactory);
                bean.getPropertyValues().add("registryBeanAutowiredType", ICollectableControllerExtendable.class);
                bean.getPropertyValues().add("beanLookup", beanLookup);
                bean.getPropertyValues().add("listener", new StringBuilderCollectableController());
                bean.getPropertyValues().add("arguments", new Object[] { StringBuilder.class });
            });
        }
    }
}
