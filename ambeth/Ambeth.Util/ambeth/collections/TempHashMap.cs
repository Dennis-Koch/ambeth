using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    public class TempHashMap<K, V> : HashMap<K, V>
    {
        public delegate bool EqualsKeysDelegate(K key, MapEntry<K, V> entry);

        public delegate int ExtractHashDelegate(K key);

        public static TempHashMap<K, V> Create(int size, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate equalsHashDelegate)
        {
            return new TempHashMap<K, V>((int)(size / DEFAULT_LOAD_FACTOR) + 1, equalsKeysDelegate, equalsHashDelegate);
        }

        protected readonly EqualsKeysDelegate equalsKeysDelegate;

        protected readonly ExtractHashDelegate extractHashDelegate;

        public TempHashMap(EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashMap(float loadFactor, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashMap(int initialCapacity, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate extractHashDelegate)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = extractHashDelegate;
        }

        public TempHashMap(int initialCapacity, float loadFactor, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate equalsHashDelegate)
            : base(initialCapacity, loadFactor)
        {
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = equalsHashDelegate;
        }

        public MapEntry<K, V>[] GetTable()
        {
            return table;
        }

        public int GetThreshold()
        {
            return threshold;
        }

        public int GetTableLengthMinusOne()
        {
            return tableLengthMinusOne;
        }

        protected override bool EqualKeys(K key, MapEntry<K, V> entry)
        {
            return equalsKeysDelegate.Invoke(key, entry);
        }

        protected override int ExtractHash(K key)
        {
            return extractHashDelegate.Invoke(key);
        }
    }
}