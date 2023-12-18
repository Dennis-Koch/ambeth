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

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Abstrakte HashSet als Basisklasse fuer verschiedene spezialisierte Anwendungsfaelle
 *
 * @param <K> Typ der Keys
 * @author kochd
 */
public abstract class AbstractHashSet<K> implements ISet<K>, IPrintable, Cloneable {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    public static final int MAXIMUM_CAPACITY = 1 << 30;

    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    protected final float loadFactor;

    protected int threshold;

    protected ISetEntry<K>[] table;

    @SuppressWarnings("rawtypes")
    public AbstractHashSet(int initialCapacity, final float loadFactor, final Class<? extends ISetEntry> entryClass) {
        this.loadFactor = loadFactor;

        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        threshold = (int) (capacity * loadFactor);
        table = createTable(entryClass, capacity);

        init();
    }

    @SuppressWarnings("unchecked")
    protected ISetEntry<K>[] createTable(final int capacity) {
        return (ISetEntry<K>[]) Array.newInstance(table.getClass().getComponentType(), capacity);
    }

    @SuppressWarnings("unchecked")
    protected ISetEntry<K>[] createTable(final Class<?> entryClass, final int capacity) {
        return (ISetEntry<K>[]) Array.newInstance(entryClass, capacity);
    }

    protected void init() {

    }

    protected int extractHash(final K key) {
        return key.hashCode();
    }

    protected int hash(int hash) {
        hash += ~(hash << 9);
        hash ^= hash >>> 14;
        hash += hash << 4;
        hash ^= hash >>> 10;
        return hash;
    }

    protected void addEntry(final int hash, final K key, final int bucketIndex) {
        ISetEntry<K>[] table = this.table;
        ISetEntry<K> e = table[bucketIndex];
        e = createEntry(hash, key, e);
        table[bucketIndex] = e;
        entryAdded(e);
        if (isResizeNeeded()) {
            resize(2 * table.length);
        }
    }

    protected boolean isResizeNeeded() {
        return size() >= threshold;
    }

    @Override
    public K get(K key) {
        final int hash = hash(extractHash(key));
        ISetEntry<K>[] table = this.table;
        final int i = hash & table.length - 1;
        ISetEntry<K> entry = table[i];

        while (entry != null) {
            if (equalKeys(key, entry)) {
                return entry.getKey();
            }
            entry = entry.getNextEntry();
        }
        return null;
    }

    protected void entryAdded(final ISetEntry<K> entry) {
        // Intended blank
    }

    protected void entryRemoved(final ISetEntry<K> entry) {
        // Intended blank
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Object[] tempArray = new Object[size];
        for (int a = 0; a < size; a++) {
            tempArray[a] = in.readObject();
        }
        addAll((K[]) tempArray);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size());

        ISetEntry<K>[] table = this.table;

        for (int a = table.length; a-- > 0; ) {
            ISetEntry<K> entry = table[a];
            while (entry != null) {
                out.writeObject(entry.getKey());
                entry = entry.getNextEntry();
            }
        }
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity. This method is
     * called automatically when the number of keys in this map reaches its threshold.
     * <p>
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets
     * threshold to Integer.MAX_VALUE. This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two; must be greater than current capacity unless
     *                    current capacity is MAXIMUM_CAPACITY (in which case value is irrelevant).
     */
    protected void resize(final int newCapacity) {
        final ISetEntry<K>[] oldTable = table;
        final int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        final ISetEntry<K>[] newTable = createTable(newCapacity);
        transfer(newTable);
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    protected void transfer(final ISetEntry<K>[] newTable) {
        final int newCapacityMinus1 = newTable.length - 1;
        ISetEntry<K>[] table = this.table;
        if (table == newTable) {
            // re-check entries on existing table
            for (int a = table.length; a-- > 0; ) {
                ISetEntry<K> entry = table[a], previous = null, next;
                while (entry != null) {
                    next = entry.getNextEntry();
                    if (isEntryValid(entry)) {
                        previous = entry;
                    } else {
                        if (entry == table[a]) {
                            // first entry in bucket
                            table[a] = next;
                        } else {
                            setNextEntry(previous, next);
                        }
                        entryRemoved(entry);
                    }
                    entry = next;
                }
            }
            return;
        }

        for (int a = table.length; a-- > 0; ) {
            ISetEntry<K> entry = table[a], next;
            while (entry != null) {
                next = entry.getNextEntry();
                if (isEntryValid(entry)) {
                    int i = entry.getHash() & newCapacityMinus1;
                    setNextEntry(entry, newTable[i]);
                    newTable[i] = entry;
                } else {
                    entryRemoved(entry);
                }
                entry = next;
            }
        }
    }

    protected boolean isEntryValid(ISetEntry<K> entry) {
        return true;
    }

    /**
     * @see java.util.Set#clear()
     */
    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }
        ISetEntry<K>[] table = this.table;

        for (int a = table.length; a-- > 0; ) {
            ISetEntry<K> entry = table[a];
            if (entry != null) {
                table[a] = null;
                while (entry != null) {
                    ISetEntry<K> nextEntry = entry.getNextEntry();
                    entryRemoved(entry);
                    entry = nextEntry;
                }
            }
        }
    }

    /**
     * Returns a shallow copy of this <tt>HashSet</tt> instance: the keys themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        AbstractHashSet<K> result = null;
        try {
            result = (AbstractHashSet<K>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw RuntimeExceptionUtil.mask(e);
        }
        for (K key : this) {
            result.add(key);
        }
        return result;
    }

    /**
     * @see java.util.Set#contains(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object key) {
        final K realKey = (K) key;
        final int hash = hash(extractHash(realKey));
        ISetEntry<K>[] table = this.table;
        final int i = hash & table.length - 1;
        ISetEntry<K> entry = table[i];

        while (entry != null) {
            if (equalKeys(realKey, entry)) {
                return true;
            }
            entry = entry.getNextEntry();
        }
        return false;
    }

    protected boolean equalKeys(final K key, final ISetEntry<K> entry) {
        return key.equals(entry.getKey());
    }

    /**
     * @see java.util.Set#add(java.lang.Object)
     */
    @Override
    public boolean add(K key) {
        return addIntern(key);
    }

    protected boolean addIntern(K key) {
        if (key == null) {
            return false;
        }
        final int hash = hash(extractHash(key));
        ISetEntry<K>[] table = this.table;
        final int i = hash & table.length - 1;

        ISetEntry<K> entry = table[i];
        while (entry != null) {
            if (equalKeys(key, entry)) {
                return false;
            }
            entry = entry.getNextEntry();
        }
        addEntry(hash, key, i);
        return true;
    }

    /**
     * @see java.util.Set#remove(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object key) {
        return removeEntryForKey((K) key) != null;
    }

    @Override
    public K removeAndGet(final K key) {
        return removeEntryForKey(key);
    }

    protected K removeEntryForKey(final K key) {
        final int hash = hash(extractHash(key));
        ISetEntry<K>[] table = this.table;
        final int i = hash & table.length - 1;
        ISetEntry<K> entry = table[i];
        if (entry != null) {
            if (equalKeys(key, entry)) {
                table[i] = entry.getNextEntry();
                K keyOfEntry = entry.getKey();
                entryRemoved(entry);
                return keyOfEntry;
            }
            ISetEntry<K> prevEntry = entry;
            entry = entry.getNextEntry();
            while (entry != null) {
                if (equalKeys(key, entry)) {
                    setNextEntry(prevEntry, entry.getNextEntry());
                    K keyOfEntry = entry.getKey();
                    entryRemoved(entry);
                    return keyOfEntry;
                }
                prevEntry = entry;
                entry = entry.getNextEntry();
            }
        }
        return null;
    }

    protected abstract void setNextEntry(final ISetEntry<K> entry, final ISetEntry<K> nextEntry);

    protected abstract ISetEntry<K> createEntry(final int hash, final K key, final ISetEntry<K> nextEntry);

    /**
     * @see java.util.Set#size()
     */
    @Override
    public abstract int size();

    /**
     * @see java.util.Set#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @see java.util.Set#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean changed = false;
        if (c instanceof List) {
            List<? extends K> list = (List<? extends K>) c;
            for (int a = 0, size = list.size(); a < size; a++) {
                changed |= addIntern(list.get(a));
            }
            return !list.isEmpty();
        }
        Iterator<? extends K> iter = c.iterator();
        while (iter.hasNext()) {
            K key = iter.next();
            changed |= addIntern(key);
        }
        return changed;
    }

    @Override
    public boolean addAll(Iterable<? extends K> c) {
        boolean changed = false;
        if (c instanceof List) {
            List<? extends K> list = (List<? extends K>) c;
            for (int a = 0, size = list.size(); a < size; a++) {
                changed |= addIntern(list.get(a));
            }
            return !list.isEmpty();
        }
        Iterator<? extends K> iter = c.iterator();
        while (iter.hasNext()) {
            K key = iter.next();
            changed |= addIntern(key);
        }
        return changed;
    }

    @Override
    public <S extends K> boolean addAll(S[] array) {
        boolean changed = false;
        for (int a = 0, size = array.length; a < size; a++) {
            changed |= addIntern(array[a]);
        }
        return changed;
    }

    /**
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        Iterator<?> iter = c.iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (!contains(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsAny(Collection<?> coll) {
        Iterator<?> iter = coll.iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (contains(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <S extends K> boolean containsAny(S[] array) {
        for (S key : array) {
            if (contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        if (c instanceof List) {
            List<?> list = (List<?>) c;
            for (int a = list.size(); a-- > 0; ) {
                changed |= remove(list.get(a));
            }
        } else {
            Iterator<?> iter = c.iterator();
            while (iter.hasNext()) {
                Object key = iter.next();
                changed |= remove(key);
            }
        }
        return changed;
    }

    @Override
    public <S extends K> boolean removeAll(S[] array) {
        boolean changed = false;
        for (int a = 0, size = array.length; a < size; a++) {
            changed |= remove(array[a]);
        }
        return changed;
    }

    /**
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<K> iter = iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (!c.contains(key)) {
                iter.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * @see java.util.Set#toArray()
     */
    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * @see java.util.Set#toArray(T[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        int index = 0;
        int length = array.length;
        ISetEntry<K>[] table = this.table;
        for (int a = table.length; a-- > 0; ) {
            ISetEntry<K> entry = table[a];
            while (entry != null) {
                if (entry.isValid()) {
                    if (length <= index) {
                        length = size();
                        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length);
                        System.arraycopy(array, 0, newArray, 0, index);
                        array = newArray;
                    }
                    array[index++] = (T) entry.getKey();
                }
                entry = entry.getNextEntry();
            }
        }
        for (int a = array.length; a-- > index; ) {
            array[a] = null;
        }
        return array;
    }

    @Override
    public IList<K> toList() {
        ArrayList<K> list = new ArrayList<>(size());
        toList(list);
        return list;
    }

    @Override
    public void toList(Collection<K> list) {
        ISetEntry<K>[] table = this.table;
        for (int a = table.length; a-- > 0; ) {
            ISetEntry<K> entry = table[a];
            while (entry != null) {
                if (entry.isValid()) {
                    list.add(entry.getKey());
                }
                entry = entry.getNextEntry();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(size()).append(" items: [");
        boolean first = true;
        Iterator<K> iter = iterator();
        while (iter.hasNext()) {
            K item = iter.next();
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            StringBuilderUtil.appendPrintable(sb, item);
        }
        sb.append(']');
    }

    @Override
    public Iterator<K> iterator() {
        return new SetIterator<>(this, true);
    }

    @Override
    public Iterator<K> iterator(boolean removeAllowed) {
        return new SetIterator<>(this, removeAllowed);
    }
}
