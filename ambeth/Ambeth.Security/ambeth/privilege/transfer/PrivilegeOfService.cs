using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Runtime.Serialization;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [DataContract(Name = "PrivilegeOfService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PrivilegeOfService : IPrivilegeOfService, IPrintable
    {
        [DataMember(IsRequired = true)]
        public IObjRef Reference { get; set; }

        [DataMember(IsRequired = true)]
        public ISecurityScope SecurityScope { get; set; }

        [DataMember(IsRequired = true)]
        public bool ReadAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool CreateAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool UpdateAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool DeleteAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool ExecuteAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public String[] PropertyPrivilegeNames { get; set; }

        [DataMember(IsRequired = true)]
        public IPropertyPrivilegeOfService[] PropertyPrivileges { get; set; }

	    public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    public void ToString(StringBuilder sb)
	    {
		    sb.Append(ReadAllowed ? "+R" : "-R");
		    sb.Append(CreateAllowed ? "+C" : "-C");
		    sb.Append(UpdateAllowed ? "+U" : "-U");
		    sb.Append(DeleteAllowed ? "+D" : "-D");
		    sb.Append(ExecuteAllowed ? "+E" : "-E");
	    }
    }
}