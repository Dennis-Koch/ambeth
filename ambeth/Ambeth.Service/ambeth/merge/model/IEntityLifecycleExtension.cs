using System;

namespace De.Osthus.Ambeth.Merge.Model
{
    public interface IEntityLifecycleExtension
    {
        void PostLoad(Object entity);

        void PrePersist(Object entity);
    }
}
