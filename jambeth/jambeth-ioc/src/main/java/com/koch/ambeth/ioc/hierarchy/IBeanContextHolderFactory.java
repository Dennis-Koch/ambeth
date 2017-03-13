package com.koch.ambeth.ioc.hierarchy;

public interface IBeanContextHolderFactory<V>
{
	IBeanContextHolder<V> create();

	IBeanContextHolder<V> create(Object... autowireableSourceBeans);
}
