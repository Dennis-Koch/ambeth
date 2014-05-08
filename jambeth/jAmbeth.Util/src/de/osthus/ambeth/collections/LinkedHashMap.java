package de.osthus.ambeth.collections;

import java.util.Map;

/**
 * Auf Performance optimierte Version der �blichen LinkedHashMap aus dem java.util-Paket. Diese Implementierung hat zus�tzlich zur LinkedMap den Vorteil,
 * dass sie a) direkt Collectable ist, also ohne Umwege �ber den ObjectCollector verwaltet werden kann. Daraus folgt, dass eine h�ufige Verwendung leerer
 * StLinkedMaps f�r kurze Zeit der Garbage f�r neue Instanzen vermieden wird b) Auch die MapEntries sind Collectable. Hier speziell sogar Collectable f�r
 * maximale Performance bei intensiver Nutzung derselben Map von wenigen Threads. ____________________________________________________________________ Diese
 * Klasse kann bedenkenlos, auch von mehreren Threads (mit zust�tzlichem und hier nicht durchgef�hrtem Sync-Aufwand nat�rlich) verwendet werden, mit einer
 * Anmerkung: Sie ist NICHT geeignet, wenn man sie in einem 'Fliessband-Pattern' einsetzt: Thread A f�gt Objekte IN die Map und Thread B entfernt und
 * bearbeitet diese wieder. Dabei w�rde im ObjectCollector von Thread B eine gro�e Menge an aufger�umten MapEntries entstehen, die vom ObjectCollector von
 * Thread A jedoch nicht verwendet werden und dieser munter neue Instanzen generiert. Hierbei w�rde fr�her oder sp�ter ein sicherer OutOfMemory entstehen.
 * 
 * @author kochd
 * @param <K>
 *            Der Typ der in der Map enthaltenen Keys
 * @param <V>
 *            Der Typ der in der Map enthaltenen Values
 */
public class LinkedHashMap<K, V> extends AbstractLinkedMap<K, V>
{
	public static <K, V> LinkedHashMap<K, V> create(int size)
	{
		return new LinkedHashMap<K, V>((int) (size / DEFAULT_LOAD_FACTOR) + 1);
	}

	public LinkedHashMap()
	{
		super(MapLinkedEntry.class);
	}

	public LinkedHashMap(float loadFactor)
	{
		super(loadFactor, MapLinkedEntry.class);
	}

	public LinkedHashMap(int initialCapacity)
	{
		super(initialCapacity, MapLinkedEntry.class);
	}

	public LinkedHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor, MapLinkedEntry.class);
	}

	public LinkedHashMap(Map<? extends K, ? extends V> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, MapLinkedEntry.class);
		putAll(map);
	}

	@Override
	protected IMapEntry<K, V> createEntry(int hash, K key, V value, IMapEntry<K, V> nextEntry)
	{
		MapLinkedEntry<K, V> entry = new MapLinkedEntry<K, V>(hash, key, value);
		entry.setNextEntry((MapLinkedEntry<K, V>) nextEntry);
		return entry;
	}
}
