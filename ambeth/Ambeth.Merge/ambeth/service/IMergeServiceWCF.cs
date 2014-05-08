using System;
using System.ServiceModel;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name = "IMergeService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(MergeServiceModelProvider))]
    public interface IMergeServiceWCF
    {
        [OperationContract]
        OriCollection Merge(CUDResult cudResult, MethodDescription methodDescription);

        [OperationContract]
        EntityMetaData[] GetMetaData(String[] entityTypeNames);

        [OperationContract]
        ValueObjectConfig GetValueObjectConfig(String valueTypeName);
    }

    [ServiceContract(Name = "IMergeService", Namespace = "http://schemas.osthus.de/Ambeth")]
    [ServiceKnownType("RegisterKnownTypes", typeof(MergeServiceModelProvider))]
    public interface IMergeClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginMerge(CUDResult cudResult, MethodDescription methodDescription, AsyncCallback callback, object asyncState);
        OriCollection EndMerge(IAsyncResult result);
        
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetMetaData(String[] entityTypeNames, AsyncCallback callback, object asyncState);
        EntityMetaData[] EndGetMetaData(IAsyncResult result);
    }

}
