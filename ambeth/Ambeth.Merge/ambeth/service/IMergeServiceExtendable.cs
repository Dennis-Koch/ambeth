using System;

namespace De.Osthus.Ambeth.Service
{
    public interface IMergeServiceExtendable
    {
        void RegisterMergeService(IMergeService mergeService, Type handledType);

        void UnregisterMergeService(IMergeService mergeService, Type handledType);
    }
}