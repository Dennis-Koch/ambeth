using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    public class IdentityWeakHashMap<K, V> : WeakHashMap<K, V>
    {
        public static new IdentityWeakHashMap<K, V> Create(int size)
        {
            return Create(size, DEFAULT_LOAD_FACTOR);
        }

        public static new IdentityWeakHashMap<K, V> Create(int size, float loadFactor)
        {
            return new IdentityWeakHashMap<K, V>((int)(size / loadFactor) + 1, loadFactor);
        }

        public IdentityWeakHashMap()
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // intended blank
        }

        public IdentityWeakHashMap(float loadFactor)
            : this(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // intended blank		
        }

        public IdentityWeakHashMap(int initialCapacity)
            : this(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // intended blank		
        }

        public IdentityWeakHashMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor, typeof(WeakMapEntry<K, V>))
        {
            // intended blank		
        }

        public IdentityWeakHashMap(IMap<K, V> map)
            : this((int)((map.Count / DEFAULT_LOAD_FACTOR) + 1), DEFAULT_LOAD_FACTOR)
        {
            PutAll(map);
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

        protected override bool EqualKeys(K key, IMapEntry<K, V> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }
    }
}