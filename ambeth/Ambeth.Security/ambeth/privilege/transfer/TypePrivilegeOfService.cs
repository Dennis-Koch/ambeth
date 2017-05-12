using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Runtime.Serialization;
using System.Text;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [DataContract(Name = "TypePrivilegeOfService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class TypePrivilegeOfService : ITypePrivilegeOfService, IPrintable
    {
        [DataMember(IsRequired = true)]
        public Type EntityType { get; set; }

        [DataMember(IsRequired = true)]
        public ISecurityScope SecurityScope { get; set; }

        [DataMember(IsRequired = true)]
        public bool? ReadAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool? CreateAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool? UpdateAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool? DeleteAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public bool? ExecuteAllowed { get; set; }

        [DataMember(IsRequired = true)]
        public String[] PropertyPrivilegeNames { get; set; }

        [DataMember(IsRequired = true)]
        public ITypePropertyPrivilegeOfService[] PropertyPrivileges { get; set; }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(ReadAllowed.HasValue ? ReadAllowed.Value ? "+R" : "-R" : "nR");
            sb.Append(CreateAllowed.HasValue ? CreateAllowed.Value ? "+C" : "-C" : "nC");
            sb.Append(UpdateAllowed.HasValue ? UpdateAllowed.Value ? "+U" : "-U" : "nU");
            sb.Append(DeleteAllowed.HasValue ? DeleteAllowed.Value ? "+D" : "-D" : "nD");
            sb.Append(ExecuteAllowed.HasValue ? ExecuteAllowed.Value ? "+E" : "-E" : "nE");
        }
    }
}