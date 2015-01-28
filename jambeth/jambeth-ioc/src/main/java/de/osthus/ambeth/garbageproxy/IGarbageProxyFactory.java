package de.osthus.ambeth.garbageproxy;

import de.osthus.ambeth.util.IDisposable;

public interface IGarbageProxyFactory
{
	<T> IGarbageProxyConstructor<T> createGarbageProxyConstructor(Class<T> interfaceType, Class<?>... additionalInterfaceTypes);

	<T> T createGarbageProxy(IDisposable target, Class<T> interfaceType, Class<?>... additionalInterfaceTypes);

	<T> T createGarbageProxy(Object target, IDisposable disposable, Class<T> interfaceType, Class<?>... additionalInterfaceTypes);
}
