using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Privilege.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface IPrivilegeService
    {
        IList<IPrivilegeOfService> GetPrivileges(IObjRef[] oris, ISecurityScope[] securityScopes);

        IList<ITypePrivilegeOfService> GetPrivilegesOfTypes(Type[] entityTypes, ISecurityScope[] securityScopes);
    }
}
