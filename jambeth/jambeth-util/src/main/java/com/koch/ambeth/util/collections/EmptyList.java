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
import com.koch.ambeth.util.IPrintable;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Hochperformante Implementierung einer Liste, welche leer ist und nicht modifiziert werden kann.
 * Der Sinn hierbei ist, dass bei R�ckgabewerten von Methoden, bei dem man �blicherweise eine neue
 * Instanz einer leeren Liste verwenden w�rde, fallspezifisch der Singleton dieser Klasse verwenden
 * werden k�nnte.
 * <p>
 * Hierbei w�rde keinerlei Garbage entstehen und der einzige Funktionalit�tsverlust w�re die
 * fehlende Modifizierbarkeit des R�ckgabewertes f�r den Aufrufer. Analysen der h�ufigsten Patterns
 * haben jedoch gezeigt, dass dieser theoretische Verlust sehr selten praktische Relevanz besitzt.
 * <p>
 * Die auf dieser Klasse aufrufbaren Methoden sind:
 * <p>
 * clear(), iterator(), isEmpty(), lastIndexOf(), contains(), containsAll(), listIterator(), size(),
 * toArray()
 * <p>
 * Alle anderen f�hren zu einer UnsupportedOperationException
 *
 * @param <V> Typ der Liste
 * @author kochd
 */
public class EmptyList<V> implements IList<V>, IPrintable, IImmutableType {
    public static final EmptyList<?> instance = new EmptyList<>();
    @SuppressWarnings("rawtypes")
    static final ListIterator emptyIter = new ListIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Object e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object previous() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object e) {
            throw new UnsupportedOperationException();
        }
    };
    private static final Object[] emptyArray = new Object[0];

    @SuppressWarnings("unchecked")
    static <K> ListIterator<K> emptyIterator() {
        return emptyIter;
    }

    @SuppressWarnings("unchecked")
    public static final <V> EmptyList<V> getInstance() {
        return (EmptyList<V>) instance;
    }

    @SuppressWarnings("unchecked")
    public static final <V> EmptyList<V> createTypedEmptyList(Class<V> referenceClass) {
        return (EmptyList<V>) instance;
    }

    public EmptyList() {
    }

    @Override
    public boolean add(V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends V> boolean addAll(T[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends V> boolean addAll(T[] array, int startIndex, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public V get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterator<V> iterator() {
        return emptyIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }

    @Override
    public ListIterator<V> listIterator() {
        return emptyIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public V peek() {
        return null;
    }

    @Override
    public V popLastElement() {
        return null;
    }

    @Override
    public V remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public <T extends V> boolean removeAll(T[] array) {
        return false;
    }

    @Override
    public <T extends V> boolean removeAll(T[] array, int startIndex, int length) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public V set(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public IList<V> subList(int fromIndex, int toIndex) {
        return this;
    }

    @Override
    public Object[] toArray() {
        return emptyArray;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return a;
    }

    @Override
    public String toString() {
        return "0 items: []";
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(toString());
    }
}
