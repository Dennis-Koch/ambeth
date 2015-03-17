using System;

namespace De.Osthus.Ambeth.Merge.Model
{
    public interface IEntityLifecycleExtension
    {
        void PostCreate(IEntityMetaData metaData, Object newEntity);

        void PostLoad(IEntityMetaData metaData, Object entity);

        void PrePersist(IEntityMetaData metaData, Object entity);
    }
}
