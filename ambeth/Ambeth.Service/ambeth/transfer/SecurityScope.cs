
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Security.Transfer
{
    [DataContract(Name = "SecurityScope", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class SecurityScope : ISecurityScope
    {
        [DataMember]
        public String Name { get; set; }

        public SecurityScope()
        {
            // Intended blank
        }

        public SecurityScope(String name)
        {
            ParamChecker.AssertNotNull(name, "name");
            this.Name = name;
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is SecurityScope))
            {
                return false;
            }
            SecurityScope other = (SecurityScope)obj;
            return Object.Equals(Name, other.Name);
        }

        public override int GetHashCode()
        {
            return Name.GetHashCode();
        }
    }
}
