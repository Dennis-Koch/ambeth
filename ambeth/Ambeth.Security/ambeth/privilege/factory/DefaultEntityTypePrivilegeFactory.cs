using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Privilege.Model.Impl;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public class DefaultEntityTypePrivilegeFactory : IEntityTypePrivilegeFactory
    {
        public ITypePrivilege CreatePrivilege(bool? create, bool? read, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
        {
            return new DefaultTypePrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
        }
    }
}