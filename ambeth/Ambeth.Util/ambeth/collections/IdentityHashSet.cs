using System;
using De.Osthus.Ambeth.Util;
using System.Runtime.CompilerServices;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public class IdentityHashSet<K> : CHashSet<K>
    {
        public static new IdentityHashSet<K> Create(int size)
        {
            return new IdentityHashSet<K>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        public IdentityHashSet()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public IdentityHashSet(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public IdentityHashSet(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public IdentityHashSet(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public IdentityHashSet(ICollection<K> sourceCollection)
            : base(sourceCollection)
        {
            // Intended blank
        }

        protected override int ExtractHash(K key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }

        protected override bool EqualKeys(K key, SetEntry<K> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }
    }
}
