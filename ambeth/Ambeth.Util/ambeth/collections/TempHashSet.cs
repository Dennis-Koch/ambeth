using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    public class TempHashSet<K> : CHashSet<K>
    {
        public delegate bool EqualsKeysDelegate(K key, SetEntry<K> entry);

        public delegate int ExtractHashDelegate(K key);

        public static TempHashSet<K> Create(int size, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate equalsHashDelegate)
        {
            return new TempHashSet<K>((int)(size / DEFAULT_LOAD_FACTOR) + 1, equalsKeysDelegate, equalsHashDelegate);
        }

        protected readonly EqualsKeysDelegate equalsKeysDelegate;

        protected readonly ExtractHashDelegate extractHashDelegate;

        public TempHashSet(EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashSet(float loadFactor, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashSet(int initialCapacity, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashSet(int initialCapacity, float loadFactor, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate equalsHashDelegate)
            : base(initialCapacity, loadFactor)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = equalsHashDelegate;
        }

        public SetEntry<K>[] GetTable()
        {
            return table;
        }

        public int GetThreshold()
        {
            return threshold;
        }

        protected override bool EqualKeys(K key, SetEntry<K> entry)
        {
            return equalsKeysDelegate.Invoke(key, entry);
        }

        protected override int ExtractHash(K key)
        {
            return extractHashDelegate.Invoke(key);
        }
    }
}