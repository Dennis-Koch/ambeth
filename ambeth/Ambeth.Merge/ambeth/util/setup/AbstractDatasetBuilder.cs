using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;

#if SILVERLIGHT
using De.Osthus.Ambeth.Util;
#else
using System.Threading;
#endif


namespace De.Osthus.Ambeth.Util.Setup
{
    public abstract class AbstractDatasetBuilder : IDatasetBuilder
    {
        private static readonly ThreadLocal<IList<Object>> INITIAL_TEST_DATASET_TL = new ThreadLocal<IList<Object>>();

        [LogInstance]
        public ILogger Log { private get; set; }

	    [Autowired]
	    protected IEntityFactory EntityFactory;

	    [Autowired]
	    protected IEntityMetaDataProvider EntityMetaDataProvider;

        protected abstract void BuildDatasetInternal();
        
        
        public IList<IDatasetBuilder> GetDependsOn()
        {
            return null;
        }

        public void BuildDataset(IList<Object> initialTestDataset)
        {
            IList<Object> oldSet = INITIAL_TEST_DATASET_TL.Value;
            INITIAL_TEST_DATASET_TL.Value = initialTestDataset;
            try
            {
                BuildDatasetInternal();
            }
            finally
            {
                if (oldSet != null)
                {
                    INITIAL_TEST_DATASET_TL.Value = oldSet;
                }
                else
                {
                    INITIAL_TEST_DATASET_TL.Value = null;
                }
            }
        }


        protected T CreateEntity<T>()
	    {
		    T entity = EntityFactory.CreateEntity<T>();
            INITIAL_TEST_DATASET_TL.Value.Add(entity);
		    return entity;
	    }
    }
}
