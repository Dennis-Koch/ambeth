namespace De.Osthus.Ambeth.Collections
{
    public class Tuple3KeyEntry<Key1, Key2, Key3, V>
    {
        private readonly Key1 key1;

        private readonly Key2 key2;

        private readonly Key3 key3;

        private Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry;

        private readonly int hash;

        private V value;

        public Tuple3KeyEntry(Key1 key1, Key2 key2, Key3 key3, V value, int hash, Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry)
        {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.value = value;
            this.hash = hash;
            this.nextEntry = nextEntry;
        }

        public int GetHash()
        {
            return hash;
        }

        public Tuple3KeyEntry<Key1, Key2, Key3, V> GetNextEntry()
        {
            return nextEntry;
        }

        public void SetNextEntry(Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry)
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

        public Key3 GetKey3()
        {
            return key3;
        }

        public void SetValue(V value)
        {
            this.value = value;
        }

        public V GetValue()
        {
            return value;
        }
    }
}