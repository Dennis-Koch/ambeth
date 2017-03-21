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

import java.util.Collection;
import java.util.Set;

/**
 * Implementierung einer &uuml;blichen HashSet mit dem Vorteil, dass sie a) direkt Collectable ist, also ohne Umwege &uuml;ber den ObjectCollector verwaltet
 * werden kann. Daraus folgt, dass eine h&auml;ufige Verwendung leerer HashSets f&uuml;r kurze Zeit der Garbage f&uuml;r neue Instanzen vermieden wird b) Auch
 * die SetEntries sind Collectable. Hier speziell sogar Collectable f&uuml;r maximale Performance bei intensiver Nutzung derselben Set von wenigen Threads.
 * ____________________________________________________________________ Diese Klasse kann bedenkenlos, auch von mehreren Threads (mit zus&auml;tzlichem und hier
 * nicht durchgef&uuml;hrtem Sync-Aufwand nat&uuml;rlich) verwendet werden, mit einer Anmerkung: Sie ist NICHT geeignet, wenn man sie in einem
 * 'Fliessband-Pattern' einsetzt: Thread A f&uuml;gt Objekte IN die Set und Thread B entfernt und bearbeitet diese wieder. Dabei w&uuml;rde im ObjectCollector
 * von Thread B eine gro&#223;e Menge an aufger&auml;umten SetEntries entstehen, die vom ObjectCollector von Thread A jedoch nicht verwendet werden und dieser
 * munter neue Instanzen generiert. Hierbei w&uuml;rde fr&uuml;her oder sp&auml;ter ein sicherer OutOfMemory entstehen.
 * 
 * @author kochd
 * @param <K>
 *            Der Typ der in der Set enthaltenen Keys
 */
public class HashSet<K> extends AbstractHashSet<K>
{
	public static <K> HashSet<K> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K> HashSet<K> create(int size, float loadFactor)
	{
		return new HashSet<K>((int) (size / loadFactor) + 1);
	}

	protected int size;

	public HashSet()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, SetEntry.class);
	}

	public HashSet(Collection<? extends K> sourceCollection)
	{
		this((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetEntry.class);
		addAll(sourceCollection);
	}

	public HashSet(K[] sourceArray)
	{
		this((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetEntry.class);
		addAll(sourceArray);
	}

	public HashSet(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, SetEntry.class);
	}

	public HashSet(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, SetEntry.class);
	}

	public HashSet(int initialCapacity, float loadFactor)
	{
		this(initialCapacity, loadFactor, SetEntry.class);
	}

	@SuppressWarnings("rawtypes")
	public HashSet(int initialCapacity, float loadFactor, Class<? extends ISetEntry> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
	}

	public HashSet(Set<? extends K> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetEntry.class);
		addAll(map);
	}

	@Override
	protected ISetEntry<K> createEntry(int hash, K key, ISetEntry<K> nextEntry)
	{
		return new SetEntry<K>(hash, key, (SetEntry<K>) nextEntry);
	}

	@Override
	protected void entryAdded(ISetEntry<K> entry)
	{
		size++;
	}

	@Override
	protected void entryRemoved(ISetEntry<K> entry)
	{
		size--;
	}

	@Override
	protected void setNextEntry(ISetEntry<K> entry, ISetEntry<K> nextEntry)
	{
		((SetEntry<K>) entry).setNextEntry((SetEntry<K>) nextEntry);
	}

	@Override
	public int size()
	{
		return size;
	}
}
