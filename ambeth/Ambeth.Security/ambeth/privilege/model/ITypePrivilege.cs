namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface ITypePrivilege
    {
        bool? CreateAllowed { get; }

        bool? ReadAllowed { get; }

        bool? UpdateAllowed { get; }

        bool? DeleteAllowed { get; }

        bool? ExecuteAllowed { get; }

        ITypePropertyPrivilege GetDefaultPropertyPrivilegeIfValid();

        ITypePropertyPrivilege GetPrimitivePropertyPrivilege(int primitiveIndex);

        ITypePropertyPrivilege GetRelationPropertyPrivilege(int relationIndex);
    }
}