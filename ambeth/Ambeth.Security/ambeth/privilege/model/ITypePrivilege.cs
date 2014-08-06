namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface ITypePrivilege
    {
        bool? CreateAllowed { get; }

        bool? ReadAllowed { get; }

        bool? UpdateAllowed { get; }

        bool? DeleteAllowed { get; }

        bool? ExecuteAllowed { get; }

        ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid();

        ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

        ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex);
    }
}