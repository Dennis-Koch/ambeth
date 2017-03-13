package com.koch.ambeth.util.collections;

import java.util.Collection;
import java.util.Set;

/**
 * Auf Performance optimierte Version der �blichen LinkedHashSet aus dem java.util-Paket. Diese Implementierung hat zus�tzlich zur LinkedSet den Vorteil, dass
 * sie
 * 
 * a) direkt Collectable ist, also ohne Umwege �ber den ObjectCollector verwaltet werden kann. Daraus folgt, dass eine h�ufige Verwendung leerer StLinkedSets
 * f�r kurze Zeit der Garbage f�r neue Instanzen vermieden wird
 * 
 * b) Auch die SetEntries sind Collectable. Hier speziell sogar Collectable f�r maximale Performance bei intensiver Nutzung derselben Set von wenigen Threads.
 * 
 * ____________________________________________________________________ Diese Klasse kann bedenkenlos, auch von mehreren Threads (mit zust�tzlichem und hier
 * nicht durchgef�hrtem Sync-Aufwand nat�rlich) verwendet werden, mit einer Anmerkung:
 * 
 * Sie ist NICHT geeignet, wenn man sie in einem 'Fliessband-Pattern' einsetzt: Thread A f�gt Objekte IN die Set und Thread B entfernt und bearbeitet diese
 * wieder.
 * 
 * Dabei w�rde im ObjectCollector von Thread B eine gro�e Menge an aufger�umten SetEntries entstehen, die vom ObjectCollector von Thread A jedoch nicht
 * verwendet werden und dieser munter neue Instanzen generiert. Hierbei w�rde fr�her oder sp�ter ein sicherer OutOfMemory entstehen.
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ der in der Set enthaltenen Keys
 */
public class LinkedHashSet<K> extends AbstractLinkedSet<K>
{
	public static <K> LinkedHashSet<K> create(int size)
	{
		return new LinkedHashSet<K>((int) (size / DEFAULT_LOAD_FACTOR) + 1);
	}

	public LinkedHashSet()
	{
		super(SetLinkedEntry.class);
	}

	public LinkedHashSet(Collection<? extends K> sourceCollection)
	{
		super((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetLinkedEntry.class);
		addAll(sourceCollection);
	}

	public LinkedHashSet(K[] sourceArray)
	{
		super((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetLinkedEntry.class);
		addAll(sourceArray);
	}

	public LinkedHashSet(float loadFactor)
	{
		super(loadFactor, SetLinkedEntry.class);
	}

	public LinkedHashSet(int initialCapacity)
	{
		super(initialCapacity, SetLinkedEntry.class);
	}

	public LinkedHashSet(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor, SetLinkedEntry.class);
	}

	public LinkedHashSet(Set<? extends K> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, SetLinkedEntry.class);
		addAll(map);
	}

	@Override
	protected ISetEntry<K> createEntry(int hash, K key, ISetEntry<K> nextEntry)
	{
		return new SetLinkedEntry<K>(hash, key, (SetLinkedEntry<K>) nextEntry);
	}
}