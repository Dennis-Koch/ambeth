using System;
using De.Osthus.Ambeth.Util;
using System.Runtime.CompilerServices;
using System.Collections.Generic;

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
    public class IdentityLinkedSet<K> : LinkedHashSet<K>
    {
        public static new  IdentityLinkedSet<K> Create(int size)
        {
            return new IdentityLinkedSet<K>((int)(size / DEFAULT_LOAD_FACTOR) + 1);
        }

        public IdentityLinkedSet()
            : base()
        {
            // Intended blank
        }

        public IdentityLinkedSet(float loadFactor)
            : base(loadFactor)
        {
            // Intended blank
        }

        public IdentityLinkedSet(int initialCapacity)
            : base(initialCapacity)
        {
            // Intended blank
        }

        public IdentityLinkedSet(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public IdentityLinkedSet(ICollection<K> map)
            : base(map)
        {
            // Intended blank
        }

        protected override int ExtractHash(K key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }

        protected override bool EqualKeys(K key, SetLinkedEntry<K> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }
    }
}