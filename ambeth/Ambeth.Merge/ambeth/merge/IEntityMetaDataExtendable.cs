using System;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityMetaDataExtendable
    {
        void RegisterEntityMetaData(IEntityMetaData entityMetaData);

        void UnregisterEntityMetaData(IEntityMetaData entityMetaData);

        void RegisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType);

        void UnregisterEntityMetaData(IEntityMetaData entityMetaData, Type entityType);
    }
}
