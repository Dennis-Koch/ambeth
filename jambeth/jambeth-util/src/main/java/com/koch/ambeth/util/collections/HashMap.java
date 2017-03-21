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

import java.util.Map;

/**
 * Implementierung einer �blichen HashMap mit dem Vorteil, dass sie
 *
 * a) direkt Collectable ist, also ohne Umwege �ber den ObjectCollector verwaltet werden kann.
 * Daraus folgt, dass eine h�ufige Verwendung leerer HashMaps f�r kurze Zeit der Garbage f�r neue
 * Instanzen vermieden wird
 *
 * b) Auch die MapEntries sind Collectable. Hier speziell sogar Collectable f�r maximale Performance
 * bei intensiver Nutzung derselben Map von wenigen Threads.
 *
 * Man k�nnte in diese Map zum Beispiel, wenn auch praxisfern, 2 Keys vom Typ Integer mit dem Wert
 * '1' in diese Map hinzuf�gen (und entsprechend verschiedenen Value-Relationen), solange die beiden
 * Integer verschiedene Integer-Instanzen darstellen.
 * ____________________________________________________________________ Diese Klasse kann
 * bedenkenlos, auch von mehreren Threads (mit zust�tzlichem und hier nicht durchgef�hrtem
 * Sync-Aufwand nat�rlich) verwendet werden, mit einer Anmerkung:
 *
 * Sie ist NICHT geeignet, wenn man sie in einem 'Fliessband-Pattern' einsetzt: Thread A f�gt
 * Objekte IN die Map und Thread B entfernt und bearbeitet diese wieder.
 *
 * Dabei w�rde im ObjectCollector von Thread B eine gro�e Menge an aufger�umten MapEntries
 * entstehen, die vom ObjectCollector von Thread A jedoch nicht verwendet werden und dieser munter
 * neue Instanzen generiert. Hierbei w�rde fr�her oder sp�ter ein sicherer OutOfMemory entstehen.
 *
 * @author kochd
 *
 * @param <K> Der Typ der in der Map enthaltenen Keys
 * @param <V> Der Typ der in der Map enthaltenen Values
 */
public class HashMap<K, V> extends AbstractHashMap<K, K, V> {
	public static <K, V> HashMap<K, V> create(int size) {
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> HashMap<K, V> create(int size, float loadFactor) {
		return new HashMap<>((int) (size / loadFactor) + 1, loadFactor);
	}

	protected int size;

	public HashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, MapEntry.class);
	}

	public HashMap(float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, MapEntry.class);
	}

	public HashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, MapEntry.class);
	}

	public HashMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, MapEntry.class);
	}

	public HashMap(int initialCapacity, float loadFactor, Class<?> entryClass) {
		super(initialCapacity, loadFactor, entryClass);
	}

	public HashMap(Map<? extends K, ? extends V> map) {
		this((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, MapEntry.class);
		putAll(map);
	}

	@Override
	protected IMapEntry<K, V> createEntry(int hash, K key, V value, final IMapEntry<K, V> nextEntry) {
		return new MapEntry<>(hash, (MapEntry<K, V>) nextEntry, key, value);
	}

	@Override
	protected void entryAdded(final IMapEntry<K, V> entry) {
		size++;
	}

	@Override
	protected void entryRemoved(final IMapEntry<K, V> entry) {
		size--;
	}

	@Override
	protected void setNextEntry(final IMapEntry<K, V> entry, final IMapEntry<K, V> nextEntry) {
		((MapEntry<K, V>) entry).setNextEntry((MapEntry<K, V>) nextEntry);
	}

	@Override
	protected V setValueForEntry(final IMapEntry<K, V> entry, V value) {
		V oldValue = entry.getValue();
		entry.setValue(value);
		return oldValue;
	}

	@Override
	public int size() {
		return size;
	}
}
