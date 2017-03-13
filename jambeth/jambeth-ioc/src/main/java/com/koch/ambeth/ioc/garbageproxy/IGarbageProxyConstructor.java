package com.koch.ambeth.ioc.garbageproxy;

import com.koch.ambeth.util.IDisposable;

public interface IGarbageProxyConstructor<T>
{
	T createInstance(IDisposable target);

	T createInstance(Object target, IDisposable disposable);
}
