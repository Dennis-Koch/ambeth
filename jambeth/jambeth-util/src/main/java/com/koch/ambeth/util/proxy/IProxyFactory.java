package com.koch.ambeth.util.proxy;

import net.sf.cglib.proxy.MethodInterceptor;

public interface IProxyFactory {
	<T> T createProxy(ClassLoader classLoader, Class<T> type, MethodInterceptor... interceptors);

	<T> T createProxy(ClassLoader classLoader, Class<T> type, Class<?>[] interfaces,
			MethodInterceptor... interceptors);

	Object createProxy(ClassLoader classLoader, Class<?>[] interfaces,
			MethodInterceptor... interceptors);

	ICascadedInterceptor wrap(Object target, ICascadedInterceptor interceptor);
}
