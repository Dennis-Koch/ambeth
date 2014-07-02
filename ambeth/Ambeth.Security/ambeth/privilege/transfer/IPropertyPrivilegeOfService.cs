namespace De.Osthus.Ambeth.Privilege.Transfer
{
    public interface IPropertyPrivilegeOfService
    {
        bool ReadAllowed { get; }

        bool CreateAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }
    }
}