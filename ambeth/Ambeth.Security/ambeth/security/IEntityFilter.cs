using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Security
{
    public interface IEntityFilter
    {
        ReadPermission CheckReadPermissionOnEntity(Object entity, IUserHandle userHandle);
    }
}
