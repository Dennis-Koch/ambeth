namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPropertyPrivilege
    {
        bool ReadAllowed { get; }

        bool CreateAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }
    }
}
