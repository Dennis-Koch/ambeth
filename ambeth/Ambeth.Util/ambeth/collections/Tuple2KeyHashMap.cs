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
    public class Tuple2KeyHashMap<Key1, Key2, V> : AbstractTuple2KeyHashMap<Key1, Key2, V>
    {
        public static Tuple2KeyHashMap<Key1, Key2, V> Create(int size)
        {
            return Create(size, DEFAULT_LOAD_FACTOR);
        }

        public static Tuple2KeyHashMap<Key1, Key2, V> Create(int size, float loadFactor)
        {
            return new Tuple2KeyHashMap<Key1, Key2, V>((int)(size / loadFactor) + 1, loadFactor);
        }

        protected int size;

        public Tuple2KeyHashMap()
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public Tuple2KeyHashMap(float loadFactor)
            : this(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public Tuple2KeyHashMap(int initialCapacity)
            : this(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public Tuple2KeyHashMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        protected override Tuple2KeyEntry<Key1, Key2, V> CreateEntry(int hash, Key1 key1, Key2 key2, V value, Tuple2KeyEntry<Key1, Key2, V> nextEntry)
        {
            return new Tuple2KeyEntry<Key1, Key2, V>(key1, key2, value, hash, nextEntry);
        }

        protected override void EntryAdded(Tuple2KeyEntry<Key1, Key2, V> entry)
        {
            size++;
        }

        protected override void EntryRemoved(Tuple2KeyEntry<Key1, Key2, V> entry)
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