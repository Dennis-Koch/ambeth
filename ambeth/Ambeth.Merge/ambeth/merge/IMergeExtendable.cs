using System;
using System.Net;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeExtendable
    {
        void RegisterMergeExtension(IMergeExtension mergeExtension, Type entityType);

        void UnregisterMergeExtension(IMergeExtension mergeExtension, Type entityType);
    }
}
