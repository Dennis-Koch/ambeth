namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPropertyPrivilege
    {
        bool CreateAllowed { get; }

        bool ReadAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }
    }
}