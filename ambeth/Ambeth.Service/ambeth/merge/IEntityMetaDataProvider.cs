using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityMetaDataProvider
    {
        IEntityMetaData GetMetaData(Type entityType);

        IEntityMetaData GetMetaData(Type entityType, bool tryOnly);

        IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes);

        IList<Type> FindMappableEntityTypes();

        IValueObjectConfig GetValueObjectConfig(Type valueType);

        IValueObjectConfig GetValueObjectConfig(String xmlTypeName);

        IList<Type> GetValueObjectTypesByEntityType(Type entityType);

        Type[] GetEntityPersistOrder();
    }
}
