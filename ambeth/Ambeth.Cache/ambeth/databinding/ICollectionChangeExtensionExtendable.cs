using System;

namespace De.Osthus.Ambeth.Databinding
{
    public interface ICollectionChangeExtensionExtendable
    {
        void RegisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Type entityType);

        void UnregisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Type entityType);
    }
}