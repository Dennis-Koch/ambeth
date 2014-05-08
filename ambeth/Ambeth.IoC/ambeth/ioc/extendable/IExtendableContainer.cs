using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface IExtendableContainer<V>
    {
        void Register(V extension);

        void Unregister(V extension);

        V[] GetExtensions();

        void GetExtensions(ICollection<V> targetExtensionList);
    }
}
