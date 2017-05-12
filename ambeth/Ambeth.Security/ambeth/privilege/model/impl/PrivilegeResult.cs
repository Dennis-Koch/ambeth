using System;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
	public class PrivilegeResult : IPrivilegeResult
	{
		private readonly String sid;

		private readonly IPrivilege[] privileges;

		public PrivilegeResult(String sid, IPrivilege[] privileges)
		{
			this.sid = sid;
			this.privileges = privileges;
		}

		public String SID
		{
			get
			{
				return sid;
			}
		}

		public IPrivilege[] GetPrivileges()
		{
			return privileges;
		}
	}
}