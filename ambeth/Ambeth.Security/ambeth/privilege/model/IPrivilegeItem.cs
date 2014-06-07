
namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPrivilegeItem
    {
        bool ReadAllowed { get; }

        bool CreateAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }
    }
}
