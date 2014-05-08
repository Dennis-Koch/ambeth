using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Privilege.Model
{
    public interface IPrivilegeResult
    {
        IObjRef Reference { get; }

        ISecurityScope SecurityScope { get; }

        PrivilegeEnum[] Privileges { get; }
    }
}
