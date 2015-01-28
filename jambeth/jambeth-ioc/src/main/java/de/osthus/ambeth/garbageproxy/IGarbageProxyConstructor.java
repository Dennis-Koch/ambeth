package de.osthus.ambeth.garbageproxy;

import de.osthus.ambeth.util.IDisposable;

public interface IGarbageProxyConstructor<T>
{
	T createInstance(IDisposable target);

	T createInstance(Object target, IDisposable disposable);
}
