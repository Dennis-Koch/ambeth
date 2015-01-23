package de.osthus.ambeth.garbageproxy;

import de.osthus.ambeth.util.IDisposable;

public interface IGarbageProxyConstructor
{
	GCProxy createInstance(Object target, IDisposable disposable);
}
