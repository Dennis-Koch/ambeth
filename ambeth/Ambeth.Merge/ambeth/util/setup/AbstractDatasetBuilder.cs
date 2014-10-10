using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using System.Threading;

namespace De.Osthus.Ambeth.ambeth.util.setup
{
    public abstract class AbstractDatasetBuilder : IDatasetBuilder
    {
        [LogInstance]
        private ILogger Log;

	    [Autowired]
	    protected IEntityFactory EntityFactory;

	    [Autowired]
	    protected IEntityMetaDataProvider EntityMetaDataProvider;

	    [Autowired]
	    protected IMergeProcess MergeProcess;

        protected ThreadLocal<IList<Object>> InitialTestDatasetTL = new ThreadLocal<IList<Object>>();

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
