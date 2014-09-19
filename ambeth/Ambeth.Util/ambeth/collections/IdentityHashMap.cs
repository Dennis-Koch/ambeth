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
     * Im Gegensatz zur normalen StHashMap behandelt diese Klasse identische Keys nicht durch k1.equals(k2), sondern durch k1 == k2 - also der Objekt-Identit�t.
     * 
     * Man k�nnte somit als Beispiel, wenn auch praxisfern, 2 Keys vom Typ Integer mit dem Wert '1' in diese Map hinzuf�gen (und entsprechend verschiedenen
     * Value-Relationen), solange die beiden Integer verschiedene Integer-Instanzen darstellen.
     * 
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
    public class IdentityHashMap<K, V> : HashMap<K, V>
    {
        public static new IdentityHashMap<K, V> Create(int size)
        {
            return new IdentityHashMap<K, V>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        public IdentityHashMap()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public IdentityHashMap(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public IdentityHashMap(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public IdentityHashMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public override IISet<K> KeySet()
        {
            IdentityLinkedSet<K> keySet = IdentityLinkedSet<K>.Create(Count);
            KeySet(keySet);
            return keySet;
        }

        protected override int ExtractHash(K key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }

        protected override bool EqualKeys(K key, MapEntry<K, V> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }
    }
}