package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.util.IDisposable;

public interface IBeanContextHolder<V> extends IDisposable
{
	V getValue();
}
