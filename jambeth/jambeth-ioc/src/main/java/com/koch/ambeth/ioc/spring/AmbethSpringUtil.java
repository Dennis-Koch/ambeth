package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.util.collections.IdentityWeakSmartCopyMap;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AmbethSpringUtil {
    private static final Map<Object, AtomicInteger> contextToSequenceMap = new IdentityWeakSmartCopyMap<>();

    public static SpringBeanHelper withContext(BeanDefinitionRegistry beanDefinitionRegistry) {
        var sequence = contextToSequenceMap.computeIfAbsent(beanDefinitionRegistry, key -> new AtomicInteger());
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
}
