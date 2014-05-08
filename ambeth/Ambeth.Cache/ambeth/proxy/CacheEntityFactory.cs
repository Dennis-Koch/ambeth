using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Proxy
{
    public class CacheEntityFactory : EntityFactory
    {
	    [LogInstance]
	    public new ILogger Log { private get; set; }

	    protected override void PostProcessEntity(Object entity, IEntityMetaData metaData)
	    {
		    if (entity is IBeanContextAware)
		    {
			    ((IBeanContextAware) entity).BeanContext = BeanContext;
		    }
		    base.PostProcessEntity(entity, metaData);
	    }
    }
}
