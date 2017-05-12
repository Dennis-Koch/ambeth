using System;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Security
{
    public interface IActionPermission
    {
        String Name { get; }

        PermissionApplyType ApplyType { get; }
    }
}
