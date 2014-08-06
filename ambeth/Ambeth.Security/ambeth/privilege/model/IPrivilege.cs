namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPrivilege
    {
        bool CreateAllowed { get; }

        bool ReadAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }

        bool ExecuteAllowed { get; }

        IPropertyPrivilege getDefaultPropertyPrivilegeIfValid();

        IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

        IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex);
    }
}