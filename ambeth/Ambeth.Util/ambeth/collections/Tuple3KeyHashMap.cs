namespace De.Osthus.Ambeth.Collections
{
    /**
     * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This is due to the fact that there is no need to instantiate
     * Tuple2 Keys for put() or get() operations. Of course the overall memory footprint is also the half compared to a normal map: There is only the entry object
     * with 2 key-fields compared to the entry object compared to 1 key-field which contains a Tuple2 Key instance
     * 
     * @param <Key1>
     * @param <Key2>
     * @param <V>
     */
    public class Tuple3KeyHashMap<Key1, Key2, Key3, V> : AbstractTuple3KeyHashMap<Key1, Key2, Key3, V>
    {
        public static Tuple3KeyHashMap<Key1, Key2, Key3, V> Create(int size)
        {
            return Create(size, DEFAULT_LOAD_FACTOR);
        }

        public static Tuple3KeyHashMap<Key1, Key2, Key3, V> Create(int size, float loadFactor)
        {
            return new Tuple3KeyHashMap<Key1, Key2, Key3, V>((int)(size / loadFactor) + 1, loadFactor);
        }

        protected int size;

        public Tuple3KeyHashMap()
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public Tuple3KeyHashMap(float loadFactor)
            : this(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public Tuple3KeyHashMap(int initialCapacity)
            : this(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public Tuple3KeyHashMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        protected override Tuple3KeyEntry<Key1, Key2, Key3, V> CreateEntry(int hash, Key1 key1, Key2 key2, Key3 key3, V value, Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry)
        {
            return new Tuple3KeyEntry<Key1, Key2, Key3, V>(key1, key2, key3, value, hash, nextEntry);
        }

        protected override void EntryAdded(Tuple3KeyEntry<Key1, Key2, Key3, V> entry)
        {
            size++;
        }

        protected override void EntryRemoved(Tuple3KeyEntry<Key1, Key2, Key3, V> entry)
        {
            size--;
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