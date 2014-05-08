using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityFactoryExtension
    {
        Type GetMappedEntityType(Type type);

        Object PostProcessMappedEntity(Type originalType, IEntityMetaData metaData, Object mappedEntity);
    }
}