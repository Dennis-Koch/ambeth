using De.Osthus.Ambeth.Model;
using System;

namespace De.Osthus.Ambeth.Security
{
    public class StringSecurityScope : ISecurityScope
    {
		public static readonly String DEFAULT_SCOPE_NAME = "defaultScope";

		public static readonly ISecurityScope DEFAULT_SCOPE = new StringSecurityScope(DEFAULT_SCOPE_NAME);

        protected readonly String name;

		public StringSecurityScope(String name)
		{
			this.name = name;
		}

		public String Name
		{
			get
			{
				return name;
			}
		}

		public override String ToString()
		{
			return Name;
		}
    }
}
