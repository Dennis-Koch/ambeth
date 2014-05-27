package de.osthus.ambeth.proxy;

import de.osthus.ambeth.ioc.IBeanContextAware;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class CacheEntityFactory extends EntityFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void postProcessEntity(Object entity, IEntityMetaData metaData)
	{
		if (entity instanceof IBeanContextAware)
		{
			((IBeanContextAware) entity).setBeanContext(beanContext);
		}
		super.postProcessEntity(entity, metaData);
	}
}
