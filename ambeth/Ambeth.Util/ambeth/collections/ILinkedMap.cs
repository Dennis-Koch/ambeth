using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface ILinkedMap<K, V> : IMap<K, V>, IEnumerable<Entry<K, V>>
    {
        new Iterator<Entry<K, V>> Iterator(bool removeAllowed);
    }
}