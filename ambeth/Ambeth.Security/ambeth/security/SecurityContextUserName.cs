using System;

namespace De.Osthus.Ambeth.Security
{
    [AttributeUsage(AttributeTargets.Parameter, Inherited = false, AllowMultiple = false)]
    public class SecurityContextUserName : Attribute
    {
		// intended blank
    }
}