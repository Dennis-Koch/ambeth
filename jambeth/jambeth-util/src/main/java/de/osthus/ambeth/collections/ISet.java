package de.osthus.ambeth.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface ISet<K> extends Set<K>
{
	Iterator<K> iterator(boolean removeAllowed);

	K get(K key);

	boolean containsAny(Collection<?> coll);

	IList<K> toList();

	<T> T[] toArray(Class<T> componentType);

	void toList(Collection<K> targetList);

	boolean addAll(Iterable<? extends K> c);

	<S extends K> boolean addAll(S[] array);

	<S extends K> boolean removeAll(S[] array);

	K removeAndGet(K key);
}