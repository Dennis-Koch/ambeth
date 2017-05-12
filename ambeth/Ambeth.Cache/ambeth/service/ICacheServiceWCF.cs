using System;
using System.Collections.Generic;
using System.ServiceModel;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Service
{
    [ServiceContract(Name="CacheService", Namespace = "http://schemas.osthus.de/Ambeth")]
    //[ServiceKnownType("RegisterKnownTypes", typeof(CacheServiceModelProvider))]
    public interface ICacheServiceWCF
    {
        [OperationContract]
        LoadContainer[] GetEntities(ObjRef[] orisToLoad);

        [OperationContract]
        ObjRelationResult[] GetRelations(ObjRelation[] objRelations);
        
        [OperationContract]
        ObjRef[] GetORIsForServiceRequest(ServiceDescription rootServiceContext);
    }

    [ServiceContract(Name = "CacheService", Namespace = "http://schemas.osthus.de/Ambeth")]
    //[ServiceKnownType("RegisterKnownTypes", typeof(CacheServiceModelProvider))]
    public interface ICacheClient : ICommunicationObject
    {
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetEntities(ObjRef[] orisToLoad, AsyncCallback callback, object asyncState);
        LoadContainer[] EndGetEntities(IAsyncResult result);

        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetRelations(ObjRelation[] objRelations, AsyncCallback callback, object asyncState);
        ObjRelationResult[] EndGetRelations(IAsyncResult result);
        
        [OperationContract(AsyncPattern = true)]
        IAsyncResult BeginGetORIsForServiceRequest(ServiceDescription rootServiceContext, AsyncCallback callback, object asyncState);
        ObjRef[] EndGetORIsForServiceRequest(IAsyncResult result);
    }

}
