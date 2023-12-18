package com.koch.ambeth.util.proxy;

import java.lang.reflect.Method;

public interface NoOp extends MethodInterceptor {
    public static final NoOp INSTANCE = new NoOp() {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return toString();
        }
    };
}
