using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
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
    public class CHashSet<K> : AbstractHashSet<SetEntry<K>, K>, ICollection<K>, ICollection
    {
        public static CHashSet<K> Create(int size)
        {
            return new CHashSet<K>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        protected int size;

        public CHashSet()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public CHashSet(ICollection<K> sourceCollection)
            : base((int)(sourceCollection.Count / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR)
        {
            AddAll(sourceCollection);
        }

        public CHashSet(IList sourceCollection)
            : base((int)(sourceCollection.Count / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR)
        {
            AddAll(sourceCollection);
        }

        public CHashSet(IList<K> sourceCollection)
            : base((int)(sourceCollection.Count / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR)
        {
            AddAll(sourceCollection);
        }

        public CHashSet(K[] sourceArray)
            : base((int)(sourceArray.Length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR)
        {
            AddAll(sourceArray);
        }

        public CHashSet(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public CHashSet(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public CHashSet(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        protected override SetEntry<K> CreateEntry(int hash, K key, SetEntry<K> nextEntry)
        {
            return new SetEntry<K>(hash, nextEntry, key);
        }

        protected override void EntryAdded(SetEntry<K> entry)
        {
            size++;
        }

        protected override void EntryRemoved(SetEntry<K> entry)
        {
            size--;
        }

        protected override int GetHashOfEntry(SetEntry<K> entry)
        {
            return entry.Hash;
        }

        public override K GetKeyOfEntry(SetEntry<K> entry)
        {
            return entry.Key;
        }
        
        public override SetEntry<K> GetNextEntry(SetEntry<K> entry)
        {
            return entry.NextEntry as SetEntry<K>;
        }

        protected override void SetNextEntry(SetEntry<K> entry, SetEntry<K> nextEntry)
        {
            entry.NextEntry = nextEntry;
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