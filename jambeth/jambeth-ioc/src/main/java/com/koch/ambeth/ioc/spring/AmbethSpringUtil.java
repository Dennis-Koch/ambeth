package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.util.collections.IdentityWeakSmartCopyMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AmbethSpringUtil {
    private static final Map<Object, ContextEntry> contextToSequenceMap = new IdentityWeakSmartCopyMap<>();

    public static SpringBeanHelper withContext(BeanDefinitionRegistry beanDefinitionRegistry) {
        var contextEntry = contextToSequenceMap.computeIfAbsent(beanDefinitionRegistry, key -> new ContextEntry());
        var sequence = contextEntry.getSequence();
        return new SpringBeanHelper() {

            @Override
            public BeanDefinitionRegistry getBeanDefinitionRegistry() {
                return beanDefinitionRegistry;
            }

            @Override
            public AtomicInteger getBeanSequence() {
                return sequence;
            }
        };
    }

    public static SpringLinkManager linkManager(ConfigurableListableBeanFactory beanFactory) {
        var springBeanHelper = withContext((BeanDefinitionRegistry) beanFactory);
        var contextEntry = contextToSequenceMap.computeIfAbsent(beanFactory, key -> new ContextEntry());
        var linkManager = contextEntry.getLinkManager();
        if (linkManager != null) {
            return linkManager;
        }
        synchronized (contextEntry) {
            linkManager = contextEntry.getLinkManager();
            if (linkManager == null) {
                var beanDef = springBeanHelper.createBeanDefinition(SpringLinkManager.class);
                linkManager = beanFactory.getBean(beanDef.getBeanName(), SpringLinkManager.class);
                contextEntry.setLinkManager(linkManager);
            }
            return linkManager;
        }
    }

    static class ContextEntry {
        @Getter
        final AtomicInteger sequence = new AtomicInteger();

        @Getter
        @Setter
        SpringLinkManager linkManager;
    }
}
