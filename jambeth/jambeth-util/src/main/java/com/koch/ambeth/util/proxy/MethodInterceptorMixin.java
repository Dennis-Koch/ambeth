package com.koch.ambeth.util.proxy;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInterceptorMixin {

        @RuntimeType
    public static Object intercept(@This Factory self,
                                   @Origin Method method,
                                   @AllArguments Object[] args,
                                   @SuperMethod(nullIfImpossible = true) Method superMethod,
                                   @Empty Object defaultValue) throws Throwable {
        var result = defaultValue;
        for (var callback : self.getCallbacks()) {
            if (callback == null) {
                continue;
            }
            var methodProxy = new MethodProxy() {
                @Override
                public Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
                    return method.invoke(target, args);
                }

                @Override
                public Object invokeSuper(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
                    throw new UnsupportedOperationException();
                }
            };
            result = ((MethodInterceptor)callback).intercept(self, method, args, methodProxy);
        }
        return result;
    }
}
