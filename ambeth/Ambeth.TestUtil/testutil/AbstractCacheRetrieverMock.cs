using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Testutil
{
    public abstract class AbstractCacheRetrieverMock : ICacheRetriever, IInitializingBean
    {
        protected readonly HashMap<IObjRef, ILoadContainer> databaseMap = new HashMap<IObjRef, ILoadContainer>();

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IObjectCopier ObjectCopier { protected get; set; }

        private bool initialized;

        public void AfterPropertiesSet()
        {
            initialized = false;
        }

        public abstract void Initialize();

        public IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
        {
            if (!initialized)
            {
                Initialize();
                initialized = true;
            }

            List<ILoadContainer> result = new List<ILoadContainer>(orisToLoad.Count);
            lock (databaseMap)
            {
                foreach (IObjRef oriToLoad in orisToLoad)
                {
                    ILoadContainer lc = databaseMap.Get(oriToLoad);
                    if (lc == null)
                    {
                        continue;
                    }
                    result.Add(lc);
                }
                result = ObjectCopier.Clone(result);
            }
            return result;
        }

        public IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
        {
            throw new System.NotImplementedException();
        }
    }
}