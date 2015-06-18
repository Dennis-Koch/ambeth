using System;

namespace De.Osthus.Ambeth.Privilege.Model.Impl
{
	public class TypePrivilegeResult : ITypePrivilegeResult
	{
		private readonly String sid;

		private readonly ITypePrivilege[] typePrivileges;

		public TypePrivilegeResult(String sid, ITypePrivilege[] typePrivileges)
		{
			this.sid = sid;
			this.typePrivileges = typePrivileges;
		}

		public String SID
		{
			get
			{
				return sid;
			}
		}

		public ITypePrivilege[] GetTypePrivileges()
		{
			return typePrivileges;
		}
	}
}