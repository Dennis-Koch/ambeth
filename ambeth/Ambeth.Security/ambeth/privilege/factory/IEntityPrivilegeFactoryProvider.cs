using System;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public interface IEntityPrivilegeFactoryProvider
    {
        IEntityPrivilegeFactory GetEntityPrivilegeFactory(Type entityType, bool create, bool read, bool update, bool delete, bool execute);
    }
}