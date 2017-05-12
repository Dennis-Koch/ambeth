using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Privilege.Model.Impl;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public class DefaultEntityPrivilegeFactory : IEntityPrivilegeFactory
    {
        public IPrivilege CreatePrivilege(bool create, bool read, bool update, bool delete, bool execute,
                IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
        {
            return new DefaultPrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
        }
    }
}