using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeServiceExtensionExtendable
    {
        void RegisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType);

        void UnregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Type entityType);
    }
}