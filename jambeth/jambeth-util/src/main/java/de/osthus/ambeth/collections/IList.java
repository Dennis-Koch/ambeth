package de.osthus.ambeth.collections;

import java.util.List;

public interface IList<V> extends List<V>
{
	<T extends V> boolean addAll(T[] array);

	<T extends V> boolean removeAll(T[] array);

	@Override
	IList<V> subList(int fromIndex, int toIndex);

	<T> T[] toArray(Class<T> componentType);
}
