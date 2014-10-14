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
        [LogInstance]
        public ILogger Log { private get; set; }

	    [Autowired]
	    protected IEntityFactory EntityFactory;

	    [Autowired]
	    protected IEntityMetaDataProvider EntityMetaDataProvider;

	    [Autowired]
	    protected IMergeProcess MergeProcess;

        private static readonly ThreadLocal<IList<Object>> InitialTestDatasetTL = new ThreadLocal<IList<Object>>();

        protected abstract void BuildDatasetInternal();
        
        
        public IList<IDatasetBuilder> GetDependsOn()
        {
            throw new NotImplementedException();
        }

        public IList<object> BuildDataset()
        {
            BeforeBuildDataset();
            try
            {
                BuildDatasetInternal();
                return InitialTestDatasetTL.Value;
            }
            finally
            {
                AfterBuildDataset();
            }
        }

        private void BeforeBuildDataset()
        {
            IList<Object> initialTestDataset = new List<Object>();
            InitialTestDatasetTL.Value = initialTestDataset;
        }

        private void AfterBuildDataset()
        {
            InitialTestDatasetTL.Value = null;
        }

        protected T CreateEntity<T>()
	    {
		    T entity = EntityFactory.CreateEntity<T>();
            InitialTestDatasetTL.Value.Add(entity);
		    return entity;
	    }
    }
}
