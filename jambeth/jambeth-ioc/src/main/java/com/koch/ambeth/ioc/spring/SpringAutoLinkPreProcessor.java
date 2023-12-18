package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.link.AutoLinkPreProcessor;
import com.koch.ambeth.ioc.link.IEventDelegate;
import com.koch.ambeth.ioc.link.ILinkConfigWithOptional;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededConfiguration;
import com.koch.ambeth.ioc.link.LinkContainer;
import com.koch.ambeth.ioc.link.SpringBeanLookup;
import com.koch.ambeth.ioc.link.SpringLinkConfigWithOptional;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpringAutoLinkPreProcessor extends AutoLinkPreProcessor implements BeanPostProcessor, ApplicationContextAware, InitializingBean, DisposableBean {

    protected final List<IStateRollback> rollbacks = new ArrayList<>();

    @Setter
    protected ApplicationContext applicationContext;
    @Setter
    protected SpringBeanLookup beanLookup;
    @Setter
    protected IProxyFactory proxyFactory;
    @Setter
    protected SpringLinkManager springLinkManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        Objects.requireNonNull(applicationContext, "applicationContext must be valid");
        Objects.requireNonNull(proxyFactory, "proxyFactory must be valid");
        Objects.requireNonNull(beanLookup, "beanLookup must be valid");
        Objects.requireNonNull(springLinkManager, "springLinkManager must be valid");
    }

    @Override
    public void destroy() throws Exception {
        synchronized (rollbacks) {
            for (int a = rollbacks.size(); a-- > 0; ) {
                rollbacks.get(a).rollback();
            }
            rollbacks.clear();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        preProcessPropertiesIntern(this::createLinkConfiguration, beanName, bean);
        return bean;
    }

    protected ILinkRegistryNeededConfiguration<?> createLinkConfiguration(Object bean) {
        //        var conf = new LinkConfiguration<>(LinkContainer.class, proxyFactory, props);
        //        synchronized (links) {
        //            links.add(conf);
        //        }

        //        var linkBeanName = LinkContainer.class.getSimpleName() + "#" + linkCounter.incrementAndGet();
        //        var linkContainer = new GenericBeanDefinition();
        //        linkContainer.setBeanClass(LinkContainer.class);
        //        linkContainer.getPropertyValues().add("extendableRegistry", extendableRegistry);
        //        linkContainer.getPropertyValues().add("extendableRegistry", extendableRegistry);
        //
        //        var beanDefinitionRegistry = (BeanDefinitionRegistry)applicationContext.getAutowireCapableBeanFactory();
        //        beanDefinitionRegistry.registerBeanDefinition(linkBeanName, linkContainer);
        //
        //        // force initialization
        //        applicationContext.getBean(linkBeanName);
        //
        //
        //        var linkContainer =
        //
        //                var linkConfiguration = linkController.createLinkConfiguration(bean);
        //
        //
        //        link
        //                ((DefaultListableBeanFactory)beanFactory).registerSingleton();
        //        addBeanConfiguration(linkConfiguration);
        //        return linkConfiguration;
        //        linkController.link()
        return new ILinkRegistryNeededConfiguration<>() {

            @Override
            public ILinkRegistryNeededConfiguration<Object> toContext(String nameOfBeanContext) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ILinkRegistryNeededConfiguration<Object> toContext(IServiceContext beanContext) {
                throw new UnsupportedOperationException();
            }

            @SneakyThrows
            @Override
            public ILinkConfigWithOptional to(Class<?> autowiredRegistryClass) {
                var link = new LinkContainer();
                link.setExtendableRegistry(extendableRegistry);
                link.setProxyFactory(proxyFactory);
                link.setListener(bean);
                link.setRegistryBeanAutowiredType(autowiredRegistryClass);
                link.setBeanLookup(beanLookup);
                link.afterPropertiesSet();

                var rollback = springLinkManager.registerLink(link);
                synchronized (rollbacks) {
                    rollbacks.add(rollback);
                }
                return new SpringLinkConfigWithOptional(link);
            }

            @Override
            public ILinkConfigWithOptional to(Object registry, IEventDelegate<Object> eventDelegate) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ILinkConfigWithOptional to(Object registry, Class<?> registryClass) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ILinkConfigWithOptional to(Object registry, String propertyName) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
