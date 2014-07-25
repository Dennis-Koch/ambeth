using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Cache
{
    public class RootCacheBridge : ICacheRetriever
    {
        protected static readonly CacheDirective committedRootCacheCD = CacheDirective.FailEarly | CacheDirective.ReturnMisses |
                CacheDirective.LoadContainerResult;

        protected static readonly CacheDirective loadContainerResultCD = CacheDirective.LoadContainerResult;

        [Autowired]
        public IRootCache CommittedRootCache { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public ICacheRetriever UncommittedCacheRetriever { protected get; set; }

        [Autowired(Optional = true)]
        public IInterningFeature InterningFeature { protected get; set; }

        [Autowired(Optional = true)]
        public ITransactionState TransactionState { protected get; set; }

        public IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
        {
            bool isTransaction = false;
            if (TransactionState != null)
            {
                isTransaction = TransactionState.IsTransactionActive;
            }
            List<ILoadContainer> result = new List<ILoadContainer>();
            if (!isTransaction)
            {
                // Allow committed root cache only OUT OF transactions to retrieve data by itself
                IList<Object> loadContainers = CommittedRootCache.GetObjects(orisToLoad, loadContainerResultCD);
                for (int a = loadContainers.Count; a-- > 0; )
                {
                    result.Add((ILoadContainer)loadContainers[a]);
                }
                InternStrings(result);
                return result;
            }
            List<IObjRef> orisToLoadWithVersion = new List<IObjRef>();
            List<IObjRef> missedOris = new List<IObjRef>();
            for (int i = orisToLoad.Count; i-- > 0; )
            {
                IObjRef ori = orisToLoad[i];
                if (ori.Version != null)
                {
                    orisToLoadWithVersion.Add(ori);
                }
                else
                {
                    missedOris.Add(ori);
                }
            }
            IList<Object> loadContainers2 = CommittedRootCache.GetObjects(orisToLoadWithVersion, committedRootCacheCD);
            for (int a = loadContainers2.Count; a-- > 0; )
            {
                ILoadContainer loadContainer = (ILoadContainer)loadContainers2[a];
                if (loadContainer == null)
                {
                    missedOris.Add(orisToLoadWithVersion[a]);
                }
                else
                {
                    result.Add(loadContainer);
                }
            }
            if (missedOris.Count > 0)
            {
                IList<ILoadContainer> uncommittedLoadContainer = UncommittedCacheRetriever.GetEntities(missedOris);
                result.AddRange(uncommittedLoadContainer);
            }
            InternStrings(result);
            return result;
        }

        public IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
        {
            IList<IObjRelation> orelToLoadWithVersion = new List<IObjRelation>();
            IList<IObjRelation> missedOrels = new List<IObjRelation>();
            for (int i = objRelations.Count; i-- > 0; )
            {
                IObjRelation orel = objRelations[i];
                if (orel.Version != null)
                {
                    orelToLoadWithVersion.Add(orel);
                }
                else
                {
                    missedOrels.Add(orel);
                }
            }
            IList<IObjRelationResult> relationResults = CommittedRootCache.GetObjRelations(orelToLoadWithVersion, committedRootCacheCD);
            List<IObjRelationResult> result = new List<IObjRelationResult>();
            for (int a = relationResults.Count; a-- > 0; )
            {
                IObjRelationResult relationResult = relationResults[a];
                if (relationResult == null)
                {
                    missedOrels.Add(orelToLoadWithVersion[a]);
                }
                else
                {
                    result.Add(relationResult);
                }
            }
            if (missedOrels.Count > 0)
            {
                IList<IObjRelationResult> uncommittedRelationResult = UncommittedCacheRetriever.GetRelations(missedOrels);
                result.AddRange(uncommittedRelationResult);
            }
            return result;
        }

        protected void InternStrings(IList<ILoadContainer> loadContainers)
        {
            if (InterningFeature == null)
            {
                // Feature is optional
                return;
            }
            for (int a = loadContainers.Count; a-- > 0; )
            {
                ILoadContainer loadContainer = loadContainers[a];
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(loadContainer.Reference.RealType);
                Object[] primitives = loadContainer.Primitives;
                InternPrimitiveMember(metaData, primitives, metaData.CreatedByMember);
                InternPrimitiveMember(metaData, primitives, metaData.UpdatedByMember);
            }
        }

        protected void InternPrimitiveMember(IEntityMetaData metaData, Object[] primitives, ITypeInfoItem member)
        {
            if (member == null)
            {
                return;
            }
            int index = metaData.GetIndexByPrimitiveName(member.Name);
            Object value = primitives[index];
            if (value is String)
            {
                Object internValue = InterningFeature.Intern(value);
                if (!Object.ReferenceEquals(value, internValue))
                {
                    primitives[index] = internValue;
                }
            }
        }
    }
}