package com.koch.ambeth.util.proxy;

import net.sf.cglib.proxy.MethodInterceptor;

public interface IProxyFactory
{
	<T> T createProxy(Class<T> type, MethodInterceptor... interceptors);

	<T> T createProxy(Class<T> type, Class<?>[] interfaces, MethodInterceptor... interceptors);

	Object createProxy(Class<?>[] interfaces, MethodInterceptor... interceptors);

	ICascadedInterceptor wrap(Object target, ICascadedInterceptor interceptor);
}
