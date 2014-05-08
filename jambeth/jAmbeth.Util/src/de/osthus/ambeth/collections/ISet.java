package de.osthus.ambeth.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface ISet<K> extends Set<K>
{
	Iterator<K> iterator(boolean removeAllowed);

	K get(K key);

	IList<K> toList();

	<T> T[] toArray(Class<T> componentType);

	void toList(Collection<K> targetList);

	<S extends K> boolean addAll(S[] array);

	K removeAndGet(K key);
}