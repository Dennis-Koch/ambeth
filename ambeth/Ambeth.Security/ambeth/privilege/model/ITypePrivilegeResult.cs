using System;

namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface ITypePrivilegeResult
    {
		String SID { get; }

		ITypePrivilege[] GetTypePrivileges();
    }
}