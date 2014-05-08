using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface IMap<K, V> : Iterable<Entry<K, V>>
    {
        void Clear();

        IISet<K> KeySet();

        void KeySet(IISet<K> targetKeySet);

        int Count { get; }

        bool IsEmpty();

        IList<V> Values();

        bool ContainsKey(K key);

        V Get(K key);

        K GetKey(K key);

        V Put(K key, V value);

        bool PutIfNotExists(K key, V value);

        V Remove(K key);

        bool RemoveIfValue(K key, V value);

        V[] ToArray();

        V[] ToArray(V[] targetArray);
    }
}