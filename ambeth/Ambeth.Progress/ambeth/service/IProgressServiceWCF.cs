using System;
using System.ServiceModel;
using System.Collections.Generic;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "IProgressService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(ProgressServiceModelProvider))]
    public interface IProgressServiceWCF
    {
        [OperationContract]
        IProgressHandle CallProgressableServiceAsync(IServiceDescription serviceDescription);

        [OperationContract]
        IResultProgress CallProgressableService(IServiceDescription serviceDescription);

        [OperationContract]
        IProgress Status(IProgressHandle progressHandle);
    }

    [ServiceContract(Name = "IProgressService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(ProgressServiceModelProvider))]
    public interface IProgressClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginCallProgressableServiceAsync(IServiceDescription serviceDescription, AsyncCallback callback, object asyncState);
        IProgress EndCallProgressableServiceAsync(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginCallProgressableService(IServiceDescription serviceDescription, AsyncCallback callback, object asyncState);
        IResultProgress EndCallProgressableService(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginStatus(IServiceDescription serviceDescription, AsyncCallback callback, object asyncState);
        IResultProgress EndStatus(IAsyncResult result);
    }
}
