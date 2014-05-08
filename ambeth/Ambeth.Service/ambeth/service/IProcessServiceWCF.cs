using System;
using De.Osthus.Ambeth.Model;
using System.ServiceModel;
using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "IProcessService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IProcessServiceWCF
    {
        [OperationContract]
        Object InvokeService(ServiceDescription serviceDescription);
    }

    [ServiceContract(Name = "IProcessService", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IProcessClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginInvokeService(ServiceDescription serviceDescription, AsyncCallback callback, object asyncState);
        Object EndInvokeService(IAsyncResult result);
    }
}
