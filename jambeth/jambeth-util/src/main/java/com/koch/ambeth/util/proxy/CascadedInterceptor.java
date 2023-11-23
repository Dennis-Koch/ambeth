package com.koch.ambeth.util.proxy;

import lombok.SneakyThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class CascadedInterceptor extends AbstractSimpleInterceptor implements ICascadedInterceptor {
    private Object target;

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public void setTarget(Object obj) {
        target = obj;
    }

    @SneakyThrows
    protected Object invokeTarget(Object obj, Method method, Object[] args, MethodProxy proxy) {
        Object target = getTarget();
        if (target instanceof MethodInterceptor) {
            return ((MethodInterceptor) target).intercept(obj, method, args, proxy);
        }
        if (target instanceof InvocationHandler) {
            return ((InvocationHandler) target).invoke(proxy, method, args);
        }
        return proxy.invoke(target, args);
    }
}
