package com.koch.ambeth.ioc.hierarchy;

import com.koch.ambeth.util.IDisposable;

public interface IBeanContextHolder<V> extends IDisposable
{
	V getValue();
}
