using System;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Proxy
{
    public abstract class AbstractEntityFactory : IEntityFactory
    {
	    [LogInstance]
        public ILogger Log { private get; set; }

	    [Autowired]
	    public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

	    public T CreateEntity<T>()
	    {
            return (T) CreateEntity(typeof(T));
	    }

        public virtual Object CreateEntity(Type entityType)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            return CreateEntity(metaData);
        }

        public abstract Object CreateEntity(IEntityMetaData metaData);

	    public virtual bool SupportsEnhancement(Type enhancementType)
	    {
		    return false;
	    }
    }
}
