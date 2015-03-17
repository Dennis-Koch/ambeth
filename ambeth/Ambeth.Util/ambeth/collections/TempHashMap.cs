using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    public class TempHashMap<E, K, V> : AbstractHashMap<E, K, V> where E : IMapEntry<K, V>
    {
        public delegate bool EqualsKeysDelegate(K key, E entry);

        public delegate int ExtractHashDelegate(K key);

        public delegate E CreateEntryDelegate(int hash, K key, V value, E nextEntry);

        public delegate void SetNextEntryDelegate(E entry, E nextEntry);

        public delegate V SetValueForEntryDelegate(E entry, V value);

        protected readonly CreateEntryDelegate createEntryDelegate;

        protected readonly EqualsKeysDelegate equalsKeysDelegate;

        protected readonly ExtractHashDelegate extractHashDelegate;

        protected readonly SetNextEntryDelegate setNextEntryDelegate;

        protected readonly SetValueForEntryDelegate setValueForEntryDelegate;

        protected int size;

        public TempHashMap(int initialCapacity, float loadFactor, CreateEntryDelegate createEntryDelegate, EqualsKeysDelegate equalsKeysDelegate, ExtractHashDelegate equalsHashDelegate,
            SetNextEntryDelegate setNextEntryDelegate, SetValueForEntryDelegate setValueForEntryDelegate)
            : base(initialCapacity, loadFactor)
        {
            this.createEntryDelegate = createEntryDelegate;
            this.equalsKeysDelegate = equalsKeysDelegate;
            this.extractHashDelegate = equalsHashDelegate;
            this.setNextEntryDelegate = setNextEntryDelegate;
            this.setValueForEntryDelegate = setValueForEntryDelegate;
        }

        public override int Count
        {
            get
            {
                return size;
            }
        }

        public E[] GetTable()
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

        protected override E CreateEntry(int hash, K key, V value, E nextEntry)
        {
            return createEntryDelegate(hash, key, value, nextEntry);
        }

        protected override void SetNextEntry(E entry, E nextEntry)
        {
            setNextEntryDelegate(entry, nextEntry);
        }

        protected override V SetValueForEntry(E entry, V value)
        {
            return setValueForEntryDelegate(entry, value);
        }

        protected override bool EqualKeys(K key, E entry)
        {
            return equalsKeysDelegate(key, entry);
        }

        protected override int ExtractHash(K key)
        {
            return extractHashDelegate(key);
        }
    }
}