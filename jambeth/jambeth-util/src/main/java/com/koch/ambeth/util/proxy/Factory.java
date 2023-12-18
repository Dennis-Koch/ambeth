package com.koch.ambeth.util.proxy;

public interface Factory {
    MethodInterceptor getInterceptor();

    void setInterceptor(MethodInterceptor interceptor);
}
