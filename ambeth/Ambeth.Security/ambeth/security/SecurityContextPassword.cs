using System;

namespace De.Osthus.Ambeth.Security
{
    [AttributeUsage(AttributeTargets.Parameter, Inherited = false, AllowMultiple = false)]
    public class SecurityContextPassword : Attribute
    {
        public PasswordType Value { get; set; }

        public SecurityContextPassword()
			: this(PasswordType.PLAIN)
        {
        }

		public SecurityContextPassword(PasswordType value)
        {
            this.Value = value;
        }
    }
}