using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [DataContract(Name = "PrivilegeResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PrivilegeResult : IPrivilegeResult
    {       

        [DataMember]
        public IObjRef Reference { get; set; }

        [DataMember]
        public ISecurityScope SecurityScope { get; set; }

        [DataMember]
        public PrivilegeEnum[] Privileges { get; set; }
    }
}
