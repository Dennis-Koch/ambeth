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

import com.koch.ambeth.util.StringBuilderUtil;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Erweiterte Map, welche zus�tzlich zu den �blichen Key/Value-Entries eine Liste aller Eintr�ge
 * verwaltet. Somit die Komplexit�t f�r das Iterieren �ber eine solchen Map mit O(n) = n identisch
 * mit jener einer �blichen Array-Liste. Der Tradeoff sind hierbei nat�rlich die leicht
 * aufw�ndigeren put()- und remove()-Operationen, welche jedoch weiterhin bzgl. der Komplexit�t mit
 * O(n) = 1 konstant bleiben.
 *
 * @param <K> Der Typ der in der Map enthaltenen Keys
 * @param <V> Der Typ der in der Map enthaltenen Values
 * @author kochd
 */
public abstract class AbstractLinkedMap<K, V> extends AbstractHashMap<K, K, V> implements ILinkedMap<K, V> {
    protected final GenericFastList<MapLinkedEntry<K, V>> fastIterationList;

    public AbstractLinkedMap(final Class<?> entryClass) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, entryClass);
    }

    public AbstractLinkedMap(final float loadFactor, final Class<?> entryClass) {
        this(DEFAULT_INITIAL_CAPACITY, loadFactor, entryClass);
    }

    public AbstractLinkedMap(final int initialCapacity, final Class<?> entryClass) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, entryClass);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractLinkedMap(final int initialCapacity, final float loadFactor, final Class<?> entryClass) {
        super(initialCapacity, loadFactor, entryClass);
        fastIterationList = new GenericFastList(entryClass);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    @Override
    public final int size() {
        return fastIterationList.size();
    }

    @Override
    protected void entryAdded(final IMapEntry<K, V> e) {
        fastIterationList.pushLast((MapLinkedEntry<K, V>) e);
    }

    @Override
    protected void entryRemoved(final IMapEntry<K, V> e) {
        fastIterationList.remove((MapLinkedEntry<K, V>) e);
    }

    @Override
    protected void transfer(final IMapEntry<K, V>[] newTable) {
        final int newCapacityMinus1 = newTable.length - 1;

        MapLinkedEntry<K, V> pointer = fastIterationList.getFirstElem(), next;
        while (pointer != null) {
            next = pointer.getNext();
            final int i = pointer.getHash() & newCapacityMinus1;
            pointer.setNextEntry((MapLinkedEntry<K, V>) newTable[i]);
            newTable[i] = pointer;
            pointer = next;
        }
    }

    @Override
    public V[] toArray(final V[] targetArray) {
        int index = 0;
        MapLinkedEntry<K, V> pointer = fastIterationList.getFirstElem();
        while (pointer != null) {
            targetArray[index++] = pointer.getValue();
            pointer = pointer.getNext();
        }
        return targetArray;
    }

    public void toList(final List<V> list) {
        MapLinkedEntry<K, V> pointer = fastIterationList.getFirstElem();
        while (pointer != null) {
            list.add(pointer.getValue());
            pointer = pointer.getNext();
        }
    }

    @Override
    protected void setNextEntry(final IMapEntry<K, V> e, final IMapEntry<K, V> nextEntry) {
        ((MapLinkedEntry<K, V>) e).setNextEntry((MapLinkedEntry<K, V>) nextEntry);
    }

    @Override
    protected V setValueForEntry(final IMapEntry<K, V> e, final V value) {
        final V oldValue = e.getValue();
        ((MapLinkedEntry<K, V>) e).value = value;
        return oldValue;
    }

    @Override
    public MapLinkedIterator<K, V> iterator() {
        return new MapLinkedIterator<>(this, true);
    }

    @Override
    public MapLinkedIterator<K, V> iterator(boolean removeAllowed) {
        return new MapLinkedIterator<>(this, removeAllowed);
    }

    @Override
    public void entrySet(Set<Entry<K, V>> targetEntrySet) {
        var iter = iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            targetEntrySet.add(entry);
        }
    }

    @Override
    public void keySet(Collection<K> targetKeySet) {
        var iter = iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            targetKeySet.add(entry.getKey());
        }
    }

    @Override
    public IList<V> values() {
        var list = new ArrayList<V>(size());
        var iter = iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            list.add(entry.getValue());
        }
        return list;
    }

    @Override
    public void clear() {
        var table = this.table;
        var tableLengthMinusOne = table.length - 1;
        MapLinkedEntry<K, V> entry = fastIterationList.getFirstElem(), next;
        while (entry != null) {
            next = entry.getNext();
            var i = entry.getHash() & tableLengthMinusOne;
            table[i] = null;
            entryRemoved(entry);
            entry = next;
        }
        fastIterationList.clear();
    }

    @Override
    public boolean containsValue(final Object value) {
        var pointer = fastIterationList.getFirstElem();

        if (value == null) {
            while (pointer != null) {
                if (pointer.getValue() == null) {
                    return true;
                }
                pointer = pointer.getNext();
            }
        } else {
            while (pointer != null) {
                if (value.equals(pointer.getValue())) {
                    return true;
                }
                pointer = pointer.getNext();
            }
        }
        return false;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(size()).append(" items: [");
        var first = true;
        var pointer = fastIterationList.getFirstElem();
        while (pointer != null) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            StringBuilderUtil.appendPrintable(sb, pointer);
            pointer = pointer.getNext();
        }
        sb.append(']');
    }
}
