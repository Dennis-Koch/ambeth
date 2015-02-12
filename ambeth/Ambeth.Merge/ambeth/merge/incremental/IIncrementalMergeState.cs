using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge.Incremental
{
    public interface IIncrementalMergeState
    {
        ICache GetStateCache();

        CreateOrUpdateContainerBuild NewCreateContainer(Type entityType);

        CreateOrUpdateContainerBuild NewUpdateContainer(Type entityType);
    }
}
