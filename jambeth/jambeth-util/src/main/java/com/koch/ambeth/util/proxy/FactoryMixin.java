package com.koch.ambeth.util.proxy;

import lombok.SneakyThrows;
import net.bytebuddy.dynamic.DynamicType;

public class FactoryMixin {

    private static final String CALLBACKS_FIELDS_NAME = "interceptor";

    @SneakyThrows
    public static <T> DynamicType.Builder<T> weave(DynamicType.Builder<T> builder) {
        return builder.implement(Factory.class).defineProperty(CALLBACKS_FIELDS_NAME, MethodInterceptor.class);
    }
}
