using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public interface IEntityTypePrivilegeFactory
    {
        ITypePrivilege CreatePrivilege(bool? read, bool? create, bool? update, bool? delete, bool? execute,
                ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges);
    }
}