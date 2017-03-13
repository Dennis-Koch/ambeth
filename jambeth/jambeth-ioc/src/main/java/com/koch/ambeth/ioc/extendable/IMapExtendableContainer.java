package com.koch.ambeth.ioc.extendable;

import java.util.Map;

import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;

public interface IMapExtendableContainer<K, V>
{
	void register(V extension, K key);

	void unregister(V extension, K key);

	V getExtension(K key);

	IList<V> getExtensions(K key);

	ILinkedMap<K, V> getExtensions();

	void getExtensions(Map<K, V> targetExtensionMap);
}
