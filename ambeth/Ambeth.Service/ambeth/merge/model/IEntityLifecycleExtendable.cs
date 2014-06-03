using System;

namespace De.Osthus.Ambeth.Merge.Model
{
    public interface IEntityLifecycleExtendable
    {
        void RegisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Type entityType);

        void UnregisterEntityLifecycleExtension(IEntityLifecycleExtension entityLifecycleExtension, Type entityType);
    }
}
