using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Security
{
    public interface IServicePermission
    {
        Regex[] Patterns { get; }

        PermissionApplyType ApplyType { get; }
    }
}
