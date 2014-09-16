using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public interface IEntityPrivilegeFactory
    {
        IPrivilege CreatePrivilege(bool read, bool create, bool update, bool delete, bool execute, IPropertyPrivilege[] primitivePropertyPrivileges,
                IPropertyPrivilege[] relationPropertyPrivileges);
    }
}