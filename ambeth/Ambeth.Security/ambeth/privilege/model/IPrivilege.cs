
namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPrivilege
    {
        bool ReadAllowed { get; }

        bool CreateAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }

        bool ExecutionAllowed { get; }
    }
}
