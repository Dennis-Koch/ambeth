package de.osthus.ambeth.ioc.extendable;

import java.util.Map;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;

public interface IMapExtendableContainer<K, V>
{
	void register(V extension, K key);

	void unregister(V extension, K key);

	V getExtension(K key);

	IList<V> getExtensions(K key);

	ILinkedMap<K, V> getExtensions();

	void getExtensions(Map<K, V> targetExtensionMap);
}
