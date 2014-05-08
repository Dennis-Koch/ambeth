using System;
using System.Net;
using System.ServiceModel;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security.Transfer;
using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "ISecurityService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(SecurityServiceModelProvider))]
    public interface ISecurityServiceWCF
    {
        [OperationContract]
        Object CallServiceInSecurityScope(SecurityScope[] securityScopes, ServiceDescription serviceDescription);
    }

    [ServiceContract(Name = "ISecurityService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(SecurityServiceModelProvider))]
    public interface ISecurityClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginCallServiceInSecurityScope(SecurityScope[] securityScopes, ServiceDescription serviceDescription, AsyncCallback callback, object asyncState);
        Object EndCallServiceInSecurityScope(IAsyncResult result);
    }

}

