using System;

namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPrivilegeResult
    {
		String SID { get; }

		IPrivilege[] GetPrivileges();
    }
}