using System;
using System.Collections.Generic;
using System.ServiceModel;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name="ILogService", Namespace="http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(LogServiceModelProvider))]
    public interface ILogService
    {
        [OperationContract]
        void Debug(String message, Exception e, DateTime logTime);

        [OperationContract]
        void Info(String message, Exception e, DateTime logTime);

        [OperationContract]
        void Warn(String message, Exception e, DateTime logTime);

        [OperationContract]
        void Error(String message, Exception e, DateTime logTime);
    }

    [ServiceContract(Name = "ILogService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(LogServiceModelProvider))]
    public interface ILogClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginDebug(String message, Exception e, DateTime logTime, AsyncCallback callback, object asyncState);
        void EndDebug(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginInfo(String message, Exception e, DateTime logTime, AsyncCallback callback, object asyncState);
        void EndInfo(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginWarn(String message, Exception e, DateTime logTime, AsyncCallback callback, object asyncState);
        void EndWarn(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginError(String message, Exception e, DateTime logTime, AsyncCallback callback, object asyncState);
        void EndError(IAsyncResult result);
    }

}
