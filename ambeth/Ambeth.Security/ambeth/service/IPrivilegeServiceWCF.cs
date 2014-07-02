using System;
using System.ServiceModel;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Security.Transfer;
using System.Collections.Generic;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Privilege.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "IPrivilegeService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(PrivilegeServiceModelProvider))]
    public interface IPrivilegeServiceWCF
    {
        //[OperationContract]
        //IList<PrivilegeResult> GetPrivileges(IObjRef[] oris, SecurityScope[] securityScopes);
    }

    [ServiceContract(Name = "IPrivilegeService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(PrivilegeServiceModelProvider))]
    public interface IPrivilegeClient : ICommunicationObject
    {
        //[OperationContract(AsyncPattern = true)]
        //IAsyncResult BeginGetPrivileges(IObjRef[] oris, SecurityScope[] securityScopes, AsyncCallback callback, object asyncState);
        //IList<PrivilegeResult> EndGetPrivileges(IAsyncResult result);
    }
}
