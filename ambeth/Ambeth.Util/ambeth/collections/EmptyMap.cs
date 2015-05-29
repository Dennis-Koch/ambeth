using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public static class EmptyMap<K, V>
    {
        public static IMap<K, V> Empty()
        {
            // Note that the static type is only instantiated when
            // it is needed, and only then is the T[0] object created, once.
            return EmptyMapIntern.Instance;
        }

        private sealed class EmptyMapIntern : IMap<K, V>, IImmutableType
        {
            public static readonly EmptyMapIntern Instance = new EmptyMapIntern();
            
            public void Clear()
            {
                throw new NotSupportedException("Set is read-only");
            }

            public IISet<K> KeySet()
            {
                return EmptySet.Empty<K>();
            }

            public void KeySet(ICollection<K> targetKeySet)
            {
                // intended blank
            }

            public IList<K> KeyList()
	        {
		        return EmptyList.Empty<K>();
	        }

            public int Count
            {
                get
                {
                    return 0;
                }
            }

            public bool IsEmpty()
            {
                return true;
            }

            public IList<V> Values()
            {
                return EmptyList.Empty<V>();
            }

            public bool ContainsKey(K key)
            {
                return false;
            }

            public V Get(K key)
            {
                return default(V);
            }

            public K GetKey(K key)
            {
                return key;
            }

            public V Put(K key, V value)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool PutIfNotExists(K key, V value)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public V Remove(K key)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool RemoveIfValue(K key, V value)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public V[] ToArray()
            {
                return EmptyList.EmptyArray<V>();
            }

            public V[] ToArray(V[] targetArray)
            {
                return targetArray;
            }

            public Iterator<Entry<K, V>> Iterator()
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }

            public Iterator<Entry<K, V>> Iterator(bool removeAllowed)
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }

            public IEnumerator<Entry<K, V>> GetEnumerator()
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }

            System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }

            Iterator Iterable.Iterator()
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }

            Iterator Iterable.Iterator(bool removeAllowed)
            {
                return EmptyIterator<Entry<K, V>>.Empty<Entry<K, V>>();
            }
        }
    }
}