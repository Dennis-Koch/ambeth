using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface IMapEntry<K, V> : Entry<K, V>
    {
        int Hash { get; }

        IMapEntry<K, V> NextEntry { get; }

        V SetValue(V value);
    }
}