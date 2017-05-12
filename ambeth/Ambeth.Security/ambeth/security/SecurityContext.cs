using System;

namespace De.Osthus.Ambeth.Security
{
    [AttributeUsage(AttributeTargets.Method | AttributeTargets.Class, Inherited = false, AllowMultiple = false)]
    public class SecurityContext : Attribute
    {
        public SecurityContextType Value { get; set; }

        public SecurityContext()
            : this(SecurityContextType.AUTHORIZED)
        {
        }

        public SecurityContext(SecurityContextType value)
        {
            this.Value = value;
        }
    }
}