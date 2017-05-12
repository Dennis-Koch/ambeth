using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IPermissionGroup
    {
        ITable Table { get; }

        ITable TargetTable { get; }

        IFieldMetaData PermissionGroupFieldOnTarget { get; }

        IFieldMetaData UserField { get; }

        IFieldMetaData PermissionGroupField { get; }

        IFieldMetaData ReadPermissionField { get; }

        IFieldMetaData UpdatePermissionField { get; }

        IFieldMetaData DeletePermissionField { get; }
    }
}