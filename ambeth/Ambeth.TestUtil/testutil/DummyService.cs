using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Testutil
{
    class DummyService : ICacheRetriever, ICacheService, IClientServiceFactory, IMergeService
    {
        public IList<Cache.Model.ILoadContainer> GetEntities(IList<Merge.Model.IObjRef> orisToLoad)
        {
            throw new NotImplementedException();
        }

        public IList<Cache.Model.IObjRelationResult> GetRelations(IList<Cache.Model.IObjRelation> objRelations)
        {
            throw new NotImplementedException();
        }

        public Type GetTargetProviderType(Type clientInterface)
        {
            throw new NotImplementedException();
        }

        public Type GetSyncInterceptorType(Type clientInterface)
        {
            throw new NotImplementedException();
        }

        public string GetServiceName(Type clientInterface)
        {
            throw new NotImplementedException();
        }

        public void PostProcessTargetProviderBean(string targetProviderBeanName, Ioc.Factory.IBeanContextFactory beanContextFactory)
        {
            throw new NotImplementedException();
        }

        public Merge.Model.IOriCollection Merge(Merge.Model.ICUDResult cudResult, Model.IMethodDescription methodDescription)
        {
            throw new NotImplementedException();
        }

        public IList<Merge.Model.IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            throw new NotImplementedException();
        }

        public Merge.IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            throw new NotImplementedException();
        }

        public Cache.Model.IServiceResult GetORIsForServiceRequest(Model.IServiceDescription serviceDescription)
        {
            throw new NotImplementedException();
        }
    }
}
