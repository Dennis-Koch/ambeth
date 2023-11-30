package com.koch.ambeth.util.proxy;

import lombok.SneakyThrows;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

public class FactoryMixin {

    private static final String CALLBACKS_FIELDS_NAME = "callbacks";

    private static Method FACTORY_GET_CALLBACK;

    private static Method FACTORY_SET_CALLBACK;

    static {
        try {
            FACTORY_GET_CALLBACK = Factory.class.getMethod("getCallback", int.class);
            FACTORY_SET_CALLBACK = Factory.class.getMethod("setCallback", int.class, Callback.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static <T> DynamicType.Builder<T> weave(DynamicType.Builder<T> builder) {
        return builder.implement(Factory.class)
                      .defineProperty(CALLBACKS_FIELDS_NAME, Callback[].class)
                      .method(ElementMatchers.anyOf(FACTORY_GET_CALLBACK, FACTORY_SET_CALLBACK))
                      .intercept(MethodDelegation.to(FactoryMixin.class));
    }

    public static Callback getCallback(@This Factory self, @Argument(0) int index) {
        return self.getCallbacks()[index];
    }

    public static void setCallback(@This Factory self, @Argument(0) int index, @Argument(1) Callback callback) {
        self.getCallbacks()[index] = callback;
    }
}
