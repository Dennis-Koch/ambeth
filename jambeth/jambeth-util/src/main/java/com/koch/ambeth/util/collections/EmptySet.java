package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.IImmutableType;

import java.util.Collection;
import java.util.Iterator;

public final class EmptySet<K> implements ISet<K>, IImmutableType {
    private static final Object[] EMPTY_OBJECTS = new Object[0];

    @SuppressWarnings("rawtypes")
    private static final EmptySet instance = new EmptySet();

    @SuppressWarnings("unchecked")
    public static <K> ISet<K> emptySet() {
        return instance;
    }

    private EmptySet() {
        // intended blank
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<K> iterator() {
        return EmptyList.emptyIterator();
    }

    @Override
    public Object[] toArray() {
        return EMPTY_OBJECTS;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return a;
    }

    @Override
    public boolean add(K e) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public <S extends K> boolean containsAny(S[] array) {
        return false;
    }

    @Override
    public boolean containsAny(Collection<?> coll) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public Iterator<K> iterator(boolean removeAllowed) {
        return EmptyList.emptyIterator();
    }

    @Override
    public K get(K key) {
        return null;
    }

    @Override
    public IList<K> toList() {
        return EmptyList.getInstance();
    }

    @Override
    public void toList(Collection<K> targetList) {
        // intended blank
    }

    @Override
    public boolean addAll(Iterable<? extends K> c) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public <S extends K> boolean addAll(S[] array) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public <S extends K> boolean removeAll(S[] array) {
        throw new UnsupportedOperationException("Set is read-only");
    }

    @Override
    public K removeAndGet(K key) {
        throw new UnsupportedOperationException("Set is read-only");
    }
}
