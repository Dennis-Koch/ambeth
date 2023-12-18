package com.koch.ambeth.util.proxy;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import lombok.SneakyThrows;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Empty;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodInterceptorMixin {

    @SneakyThrows
    public static <T> DynamicType.Builder<T> weave(DynamicType.Builder<T> builder, Class<?> superType, Type... interfaceTypes) {
        for (var interfaceType : interfaceTypes) {
            if (Factory.class.equals(interfaceType)) {
                // already implemented by FactoryMixin
                continue;
            }
            builder = builder.implement(interfaceType);
        }
        return builder.method(ElementMatchers.noneOf(Factory.class.getMethods())).intercept(MethodDelegation.to(MethodInterceptorMixin.class));
    }

    @RuntimeType
    public static Object intercept(@This Factory self, @Origin Method method, @AllArguments Object[] args, @SuperMethod(nullIfImpossible = true) Method superMethod, @Empty Object defaultValue)
            throws Throwable {
        try {
            var result = defaultValue;
            var callback = self.getInterceptor();
            var methodProxy = new MethodProxy() {
                @Override
                public Object invoke(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
                    return method.invoke(target, args);
                }

                @Override
                public Object invokeSuper(Object target, Object... args) throws InvocationTargetException, IllegalAccessException {
                    return superMethod.invoke(target, args);
                }
            };
            result = callback.intercept(self, method, args, methodProxy);
            return result;
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
        }
    }
}
