package com.koch.ambeth.util.proxy;

import java.lang.reflect.InvocationTargetException;

public interface MethodProxy {
    Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException;

    Object invokeSuper(Object target, Object... args) throws InvocationTargetException, IllegalAccessException;
}
