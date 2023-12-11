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
import lombok.SneakyThrows;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

public class ArrayList<V> implements IList<V>, Externalizable, IPrintable, Cloneable {
    private static final Object[] emptyArray = new Object[0];
    protected Object[] array = emptyArray;
    protected int size;

    public ArrayList() {
        this(10);
    }

    public ArrayList(final Collection<? extends V> coll) {
        init(coll.toArray(), coll.size());
    }

    public ArrayList(final Iterable<? extends V> coll) {
        this(10);
        for (var item : coll) {
            add(item);
        }
    }

    public ArrayList(final Object[] array) {
        init(array, array.length);
    }

    public ArrayList(final int iincStep) {
        init(new Object[iincStep], 0);
    }

    protected void init(final Object[] array, final int size) {
        this.array = array;
        this.size = size;
    }

    @Override
    public final boolean add(final V value) {
        var size = this.size;
        var array = this.array;
        if (size == array.length) {
            var buff = new Object[(array.length << 1) + 7];
            System.arraycopy(array, 0, buff, 0, size);
            array = buff;
            this.array = array;
        }
        array[size++] = value;
        this.size = size;
        return true;
    }

    @Override
    public final boolean remove(final Object value) {
        var size = this.size;
        var array = this.array;
        if (value == null) {
            for (var a = 0; a < size; a++) {
                if (array[a] == null) {
                    removeAtIndex(a);
                    return true;
                }
            }
        } else {
            for (var a = 0; a < size; a++) {
                var item = array[a];
                if (Objects.equals(value, item)) {
                    removeAtIndex(a);
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean hasValue(final V value) {
        var size = this.size;
        var array = this.array;
        if (value == null) {
            for (var a = 0; a < size; a++) {
                if (array[a] == null) {
                    return true;
                }
            }
        } else {
            for (var a = 0; a < size; a++) {
                var item = array[a];
                if (Objects.equals(value, item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final int index) {
        return (V) array[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public final V peek() {
        int size = this.size;
        if (size > 0) {
            return (V) array[size - 1];
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final V popLastElement() {
        var size = this.size;
        if (size > 0) {
            var array = this.array;
            var elem = (V) array[--size];
            array[size] = null;
            this.size = size;
            return elem;
        }
        return null;
    }

    public final void clearFrom(final int index) {
        var size = this.size;
        var array = this.array;
        for (int a = index; a < size; a++) {
            array[a] = null;
        }
        this.size = index;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance. (The elements themselves are not
     * copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    @SneakyThrows
    @Override
    public Object clone() {
        @SuppressWarnings("unchecked") var v = (ArrayList<V>) super.clone();
        v.array = Arrays.copyOf(array, size);
        v.size = size;
        return v;
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final void clear() {
        clearFrom(0);
    }

    @SuppressWarnings("unchecked")
    public final void copyInto(final ArrayList<V> otherList) {
        otherList.size = 0;
        var size = this.size;
        var array = this.array;
        for (int a = 0; a < size; a++) {
            otherList.add((V) array[a]);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(final int index) {
        var object = (V) array[index];
        removeAtIndex(index);
        return object;
    }

    public void removeAtIndex(final int index) {
        var size = this.size;
        var array = this.array;
        for (int a = index, sizeA = size - 1; a < sizeA; a++) {
            array[a] = array[a + 1];
        }
        size--;
        this.size = size;
        array[size] = null;
    }

    @Override
    public void add(final int index, final V element) {
        var size = this.size;
        var array = this.array;
        if (size == array.length) {
            var buff = new Object[(array.length << 1) + 7];
            System.arraycopy(array, 0, buff, 0, size);
            array = buff;
            this.array = array;
        }
        for (int a = size + 1, i = index + 1; a-- > i; ) {
            array[a] = array[a - 1];
        }
        array[index] = element;
        size++;
        this.size = size;
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        if (c instanceof List) {
            var list = (List<? extends V>) c;

            var listSize = list.size();
            var size = this.size;
            var array = this.array;
            if (size + listSize > array.length) {
                var sizeNeeded = size + listSize;
                var newSize = array.length << 1;
                if (newSize == 0) {
                    newSize = 1;
                }
                while (newSize < sizeNeeded) {
                    newSize = newSize << 1;
                }
                var buff = new Object[newSize + 7];
                System.arraycopy(array, 0, buff, 0, size);
                array = buff;
                this.array = array;
            }

            for (int a = 0, sizeA = list.size(); a < sizeA; a++) {
                array[size++] = list.get(a);
            }
            this.size = size;
        } else {
            var iter = c.iterator();
            while (iter.hasNext()) {
                add(iter.next());
            }
        }
        return !c.isEmpty();
    }

    @Override
    public <T extends V> boolean addAll(final T[] externArray) {
        if (externArray == null) {
            return false;
        }
        return addAll(externArray, 0, externArray.length);
    }

    @Override
    public <T extends V> boolean addAll(T[] externArray, int startIndex, int length) {
        if (externArray == null) {
            return false;
        }
        var size = this.size;
        var array = this.array;

        if (size + length > array.length) {
            var sizeNeeded = size + length;
            var newSize = array.length << 1;
            while (newSize < sizeNeeded) {
                newSize = newSize << 1;
            }
            var buff = new Object[newSize + 7];
            System.arraycopy(array, 0, buff, 0, size);
            array = buff;
            this.array = array;
        }

        for (int a = startIndex, endIndex = startIndex + length; a < endIndex; a++) {
            var item = externArray[a];
            array[size++] = item;
        }
        this.size = size;
        return length > 0;
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends V> c) {
        var currIndex = index;
        for (var item : c) {
            add(currIndex, item);
            currIndex++;
        }
        return !c.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        var iter = c.iterator();
        while (iter.hasNext()) {
            var item = iter.next();
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int indexOf(final Object o) {
        var size = this.size;
        var array = this.array;
        for (var a = 0; a < size; a++) {
            var item = array[a];
            if (o == null) {
                if (item == null) {
                    return a;
                }
            } else if (Objects.equals(o, item)) {
                return a;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<V> iterator() {
        return new FastIterator<>(this, 0);
    }

    @Override
    public int lastIndexOf(final Object o) {
        for (var a = size(); a-- > 0; ) {
            if (Objects.equals(get(a), o)) {
                return a;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<V> listIterator() {
        return new FastIterator<>(this, 0);
    }

    @Override
    public ListIterator<V> listIterator(final int index) {
        return new FastIterator<>(this, index);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean removeAll(final Collection<?> c) {
        var oneRemoved = false;
        if (c instanceof List) {
            var list = (List) c;
            for (var a = list.size(); a-- > 0; ) {
                if (remove(list.get(a))) {
                    oneRemoved = true;
                }
            }
            return oneRemoved;
        }
        var iter = c.iterator();
        while (iter.hasNext()) {
            if (remove(iter.next())) {
                oneRemoved = true;
            }
        }
        return oneRemoved;
    }

    @Override
    public <T extends V> boolean removeAll(final T[] externArray) {
        if (externArray == null) {
            return false;
        }
        return removeAll(externArray, 0, externArray.length);
    }

    @Override
    public <T extends V> boolean removeAll(T[] externArray, int startIndex, int length) {
        if (externArray == null) {
            return false;
        }
        var oneRemoved = false;
        for (int a = startIndex, endIndex = startIndex + length; a < endIndex; a++) {
            var item = externArray[a];
            oneRemoved |= remove(item);
        }
        return oneRemoved;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        if (c.size() == 0) {
            if (isEmpty()) {
                return false;
            }
            clear();
            return true;
        }
        var changed = false;
        if (c instanceof Set) {
            var array = this.array;
            var newArray = new Object[c.size()];
            var newSize = 0;
            for (int a = 0, size = size(); a < size; a++) {
                var item = array[a];
                if (c.contains(item)) {
                    newArray[newSize] = item;
                    newSize++;
                } else {
                    changed = true;
                }
            }
            this.array = newArray;
            size = newSize;
            return changed;
        }
        var array = this.array;
        for (int a = size(); a-- > 0; ) {
            if (!c.contains(array[a])) {
                changed = true;
                remove(a);
            }
        }
        return changed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V set(final int index, final V element) {
        var array = this.array;
        var oldElement = (V) array[index];
        array[index] = element;
        return oldElement;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IList<V> subList(final int fromIndex, final int toIndex) {
        var array = this.array;
        final ArrayList<V> sublist = new ArrayList<>(toIndex - fromIndex);
        for (int a = fromIndex; a < toIndex; a++) {
            sublist.add((V) array[a]);
        }
        return sublist;
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] targetArray) {
        var size = this.size;
        var array = this.array;
        if (targetArray.length < size) {
            targetArray = (T[]) Array.newInstance(targetArray.getClass().getComponentType(), size);
        }
        for (int a = size; a-- > 0; ) {
            targetArray[a] = (T) array[a];
        }
        for (int a = targetArray.length; a-- > size; ) {
            targetArray[a] = null;
        }
        return targetArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(final Class<T> componentType) {
        var array = (T[]) Array.newInstance(componentType, size());
        return toArray(array);
    }

    @Override
    public void readExternal(final ObjectInput arg0) throws IOException, ClassNotFoundException {
        var size = arg0.readInt();
        Object[] array = null;
        if (size > 0) {
            array = new Object[size];
            for (int a = 0; a < size; a++) {
                array[a] = arg0.readObject();
            }
        } else {
            array = new Object[0];
        }
        this.array = array;
        this.size = size;
    }

    @Override
    public void writeExternal(final ObjectOutput arg0) throws IOException {
        var size = this.size;
        var array = this.array;
        arg0.writeInt(size);
        for (var a = 0; a < size; a++) {
            arg0.writeObject(array[a]);
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        var size = size();
        sb.append(size).append(" items: [");
        for (var a = 0; a < size; a++) {
            if (a > 0) {
                sb.append(',');
            }
            StringBuilderUtil.appendPrintable(sb, get(a));
        }
        sb.append(']');
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getBackingArray() {
        return (T[]) array;
    }

    public static class FastIterator<V> implements ListIterator<V> {
        protected final ArrayList<V> list;
        protected int index;

        public FastIterator(ArrayList<V> list, int index) {
            this.list = list;
            this.index = index - 1;
        }

        @Override
        public boolean hasNext() {
            return list.size() > index + 1;
        }

        @Override
        public V next() {
            return list.get(++index);
        }

        @Override
        public void remove() {
            list.remove(index--);
        }

        @Override
        public void add(final V o) {
            list.add(index++, o);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public int nextIndex() {
            return index + 1;
        }

        @Override
        public V previous() {
            return list.get(--index);
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void set(final V o) {
            list.set(index, o);
        }
    }
}
