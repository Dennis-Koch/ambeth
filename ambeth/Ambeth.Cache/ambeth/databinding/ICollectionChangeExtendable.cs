using System;

namespace De.Osthus.Ambeth.Databinding
{
    public interface ICollectionChangeExtendable
    {
        void RegisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Type entityType);

        void UnregisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Type entityType);
    }
}