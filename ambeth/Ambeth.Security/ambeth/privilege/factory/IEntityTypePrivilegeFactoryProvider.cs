using System;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public interface IEntityTypePrivilegeFactoryProvider
    {
        IEntityTypePrivilegeFactory GetEntityTypePrivilegeFactory(Type entityType, bool? create, bool? read, bool? update, bool? delete, bool? execute);
    }
}