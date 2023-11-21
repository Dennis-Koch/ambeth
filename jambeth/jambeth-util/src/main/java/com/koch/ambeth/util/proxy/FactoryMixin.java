package com.koch.ambeth.util.proxy;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

public class FactoryMixin {

    private static final String CALLBACKS_FIELDS_NAME = "callbacks";

    @SneakyThrows
    public static <T> DynamicType.Builder<T> weave(DynamicType.Builder<T> builder) {
        return builder
                .implement(Factory.class)
                .defineProperty(CALLBACKS_FIELDS_NAME, Callback[].class)
                .method(ElementMatchers.is(Factory.class.getMethod("getCallback", int.class)))
                .intercept(MethodDelegation.to(FactoryMixin.class));
    }

    public static Callback getCallback(@This Factory self, @Argument(0) int index) {
        return self.getCallbacks()[index];
    }

    public static void setCallback(@This Factory self, @Argument(0) int index, @Argument(1) Callback callback) {
        self.getCallbacks()[index] = callback;
    }
}
