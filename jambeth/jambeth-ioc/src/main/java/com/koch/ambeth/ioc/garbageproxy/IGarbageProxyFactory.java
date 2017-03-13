package com.koch.ambeth.ioc.garbageproxy;

import com.koch.ambeth.util.IDisposable;

public interface IGarbageProxyFactory
{
	<T> IGarbageProxyConstructor<T> createGarbageProxyConstructor(Class<T> interfaceType, Class<?>... additionalInterfaceTypes);

	<T> T createGarbageProxy(IDisposable target, Class<T> interfaceType, Class<?>... additionalInterfaceTypes);

	<T> T createGarbageProxy(Object target, IDisposable disposable, Class<T> interfaceType, Class<?>... additionalInterfaceTypes);
}
