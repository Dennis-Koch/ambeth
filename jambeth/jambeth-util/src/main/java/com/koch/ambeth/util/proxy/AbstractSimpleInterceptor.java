package com.koch.ambeth.util.proxy;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractSimpleInterceptor implements MethodInterceptor {
    public static final Method finalizeMethod;

    public static final Method equalsMethod;

    public static final Method hashCodeMethod;

    public static final Method toStringMethod;

    static {
        try {
            equalsMethod = Object.class.getDeclaredMethod("equals", Object.class);
            hashCodeMethod = Object.class.getDeclaredMethod("hashCode");
            finalizeMethod = Object.class.getDeclaredMethod("finalize");
            toStringMethod = Object.class.getDeclaredMethod("toString");
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public final Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        try {
            if (finalizeMethod.equals(method)) {
                // Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
                return null;
            }
            if (equalsMethod.equals(method) && args[0] == obj) {
                // Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
                return Boolean.TRUE;
            }
            if (hashCodeMethod.equals(method)) {
                return proxy.invokeSuper(obj, args);
            }
            return interceptIntern(obj, method, args, proxy);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
        }
    }

    protected abstract Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}
