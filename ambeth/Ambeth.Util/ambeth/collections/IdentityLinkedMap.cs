using System;
using De.Osthus.Ambeth.Util;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * Eine Identity-Variante der LinkedMap
     * 
     * @author kochd
     * 
     * @param <K>
     *            Der Typ der in der Map enthaltenen Keys
     * @param <V>
     *            Der Typ der in der Map enthaltenen Values
     */
    public class IdentityLinkedMap<K, V> : LinkedHashMap<K, V>
    {
        public static new IdentityLinkedMap<K, V> Create(int size)
        {
            return new IdentityLinkedMap<K, V>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        public IdentityLinkedMap()
            : base()
        {
            // Intended blank
        }

        public IdentityLinkedMap(float loadFactor)
            : base(loadFactor)
        {
            // Intended blank
        }

        public IdentityLinkedMap(int initialCapacity)
            : base(initialCapacity)
        {
            // Intended blank
        }

        public IdentityLinkedMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public IdentityLinkedMap(IMap<K, V> map)
            : base((int)(map.Count / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR)
        {
            PutAll(map);
        }

        protected override int ExtractHash(K key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }

        protected override bool EqualKeys(K key, MapLinkedEntry<K, V> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }

        public override IISet<K> KeySet()
        {
            IdentityLinkedSet<K> keySet = IdentityLinkedSet<K>.Create(Count);
            KeySet(keySet);
            return keySet;
        }
    }
}