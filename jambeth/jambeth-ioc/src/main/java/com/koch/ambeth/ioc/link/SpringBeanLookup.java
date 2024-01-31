package com.koch.ambeth.ioc.link;

import com.koch.ambeth.ioc.IServiceLookup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@RequiredArgsConstructor
public class SpringBeanLookup implements IServiceLookup {

    @NonNull
    protected ApplicationContext applicationContext;

    public Object getService(String serviceName) {
        return applicationContext.getBean(serviceName);
    }

    public Object getService(String serviceName, boolean checkExistence) {
        if (!checkExistence && !applicationContext.containsBean(serviceName)) {
            return null;
        }
        return applicationContext.getBean(serviceName);
    }

    public <V> V getService(String serviceName, Class<V> targetType) {
        return applicationContext.getBean(serviceName, targetType);
    }

    public <V> V getService(String serviceName, Class<V> targetType, boolean checkExistence) {
        if (!checkExistence && !applicationContext.containsBean(serviceName)) {
            return null;
        }
        return applicationContext.getBean(serviceName, targetType);
    }

    public <T> T getService(Class<T> type) {
        return applicationContext.getBean(type);
    }

    public <T> T getService(Class<T> type, boolean checkExistence) {
        if (!checkExistence && applicationContext.getBeansOfType(type).isEmpty()) {
            return null;
        }
        return applicationContext.getBean(type);
    }
}
