using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * Implementierung einer �blichen HashMap mit dem Vorteil, dass sie
     * 
     * a) direkt Collectable ist, also ohne Umwege �ber den ObjectCollector verwaltet werden kann. Daraus folgt, dass eine h�ufige Verwendung leerer HashMaps
     * f�r kurze Zeit der Garbage f�r neue Instanzen vermieden wird
     * 
     * b) Auch die MapEntries sind Collectable. Hier speziell sogar Collectable f�r maximale Performance bei intensiver Nutzung derselben Map von wenigen Threads.
     * 
     * Man k�nnte in diese Map zum Beispiel, wenn auch praxisfern, 2 Keys vom Typ Integer mit dem Wert '1' in diese Map hinzuf�gen (und entsprechend
     * verschiedenen Value-Relationen), solange die beiden Integer verschiedene Integer-Instanzen darstellen.
     * ____________________________________________________________________ Diese Klasse kann bedenkenlos, auch von mehreren Threads (mit zust�tzlichem und hier
     * nicht durchgef�hrtem Sync-Aufwand nat�rlich) verwendet werden, mit einer Anmerkung:
     * 
     * Sie ist NICHT geeignet, wenn man sie in einem 'Fliessband-Pattern' einsetzt: Thread A f�gt Objekte IN die Map und Thread B entfernt und bearbeitet diese
     * wieder.
     * 
     * Dabei w�rde im ObjectCollector von Thread B eine gro�e Menge an aufger�umten MapEntries entstehen, die vom ObjectCollector von Thread A jedoch nicht
     * verwendet werden und dieser munter neue Instanzen generiert. Hierbei w�rde fr�her oder sp�ter ein sicherer OutOfMemory entstehen.
     * 
     * @author kochd
     * 
     * @param <K>
     *            Der Typ der in der Map enthaltenen Keys
     * @param <V>
     *            Der Typ der in der Map enthaltenen Values
     */
    public class HashMap<K, V> : AbstractHashMap<MapEntry<K, V>, K, V>, IEnumerable<Entry<K, V>>
    {
        public static HashMap<K, V> Create(int size)
        {
            return new HashMap<K, V>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        protected int size;

        public HashMap()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public HashMap(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public HashMap(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public HashMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        protected override MapEntry<K, V> CreateEntry(int hash, K key, V value, MapEntry<K, V> nextEntry)
        {
            return new MapEntry<K, V>(hash, nextEntry, key, value);
        }

        protected override void EntryAdded(MapEntry<K, V> entry)
        {
            size++;
        }

        protected override void EntryRemoved(MapEntry<K, V> entry)
        {
            size--;
        }
        
        protected override void SetNextEntry(MapEntry<K, V> entry, MapEntry<K, V> nextEntry)
        {
            entry.NextEntryReal = nextEntry;
        }

        protected override V SetValueForEntry(MapEntry<K, V> entry, V value)
        {
            return entry.SetValue(value);
        }

        public override int Count
        {
            get
            {
                return size;
            }
        }
    }
}