using System;
namespace De.Osthus.Ambeth.Collections
{
    public class Tuple2KeyEntry<Key1, Key2, V>
    {
        private readonly Key1 key1;

        private readonly Key2 key2;

        private Tuple2KeyEntry<Key1, Key2, V> nextEntry;

        private readonly int hash;

        private V value;

        public Tuple2KeyEntry(Key1 key1, Key2 key2, V value, int hash, Tuple2KeyEntry<Key1, Key2, V> nextEntry)
        {
            this.key1 = key1;
            this.key2 = key2;
            this.value = value;
            this.hash = hash;
            this.nextEntry = nextEntry;
        }

        public int GetHash()
        {
            return hash;
        }

        public Tuple2KeyEntry<Key1, Key2, V> GetNextEntry()
        {
            return nextEntry;
        }

        public void SetNextEntry(Tuple2KeyEntry<Key1, Key2, V> nextEntry)
        {
            this.nextEntry = nextEntry;
        }

        public Key1 GetKey1()
        {
            return key1;
        }

        public Key2 GetKey2()
        {
            return key2;
        }

        public void SetValue(V value)
        {
            this.value = value;
        }

        public V GetValue()
        {
            return value;
        }

	    public override String ToString()
	    {
            return "(" + GetKey1() + ":" + GetKey2() + "," + GetValue();
	    }
    }
}