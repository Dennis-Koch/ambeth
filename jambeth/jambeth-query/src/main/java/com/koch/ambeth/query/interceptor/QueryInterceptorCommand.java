package com.koch.ambeth.query.interceptor;

import com.koch.ambeth.util.proxy.MethodProxy;

import java.lang.reflect.Method;

public interface QueryInterceptorCommand {
    Object intercept(QueryInterceptor interceptor, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}
