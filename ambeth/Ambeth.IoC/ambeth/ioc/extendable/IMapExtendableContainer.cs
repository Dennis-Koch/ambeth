using De.Osthus.Ambeth.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface IMapExtendableContainer<K, V>
    {
        void Register(V extension, K key);

        void Unregister(V extension, K key);

        V GetExtension(K key);

        IList<V> GetExtensions(K key);

        ILinkedMap<K, V> GetExtensions();

        void GetExtensions(IMap<K, V> targetExtensionMap);
    }
}
