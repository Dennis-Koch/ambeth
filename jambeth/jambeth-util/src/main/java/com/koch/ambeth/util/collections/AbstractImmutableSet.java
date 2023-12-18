package com.koch.ambeth.util.collections;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractImmutableSet<K> implements ISet<K> {

    @Override
    public Iterator<K> iterator() {
        return iterator(false);
    }

    @Override
    public boolean containsAny(Collection<?> coll) {
        for (var item : coll) {
            if (contains(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <S extends K> boolean containsAny(S[] array) {
        for (var item : array) {
            var entry = get(item);
            if (entry != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IList<K> toList() {
        var list = new ArrayList<K>(size());
        toList(list);
        return list;
    }

    @Override
    public void toList(Collection<K> targetList) {
        var iter = iterator(false);
        while (iter.hasNext()) {
            var entry = iter.next();
            targetList.add(entry);
        }
    }

    @Override
    public boolean addAll(Iterable<? extends K> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends K> boolean addAll(S[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends K> boolean removeAll(S[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K removeAndGet(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        var array = new Object[size()];
        return toArray(array);
    }

    @Override
    public <T> T[] toArray(T[] array) {
        var iter = iterator();
        int index = 0;
        while (iter.hasNext()) {
            var entry = iter.next();
            array[index++] = (T) entry;
        }
        if (index == array.length) {
            return array;
        }
        // the size() metadata could be larger due to non-GC-collected entries
        var newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), index);
        System.arraycopy(array, 0, newArray, 0, index);
        return newArray;
    }

    @Override
    public boolean add(K kvEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (var item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
