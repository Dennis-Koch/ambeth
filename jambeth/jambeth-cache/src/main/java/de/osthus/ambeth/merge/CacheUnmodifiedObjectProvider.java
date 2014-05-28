package de.osthus.ambeth.merge;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.util.ParamChecker;

public class CacheUnmodifiedObjectProvider implements IUnmodifiedObjectProvider, IInitializingBean
{

	protected ICache cache;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(this.cache, "Cache");
		ParamChecker.assertNotNull(this.entityMetaDataProvider, "EntityMetaDataProvider");
	}

	public ICache getCache()
	{
		return cache;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public IEntityMetaDataProvider getEntityMetaDataProvider()
	{
		return entityMetaDataProvider;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public Object getUnmodifiedObject(Class<?> type, Object id)
	{
		return this.cache.getObject(type, id);
	}

	@Override
	public Object getUnmodifiedObject(Object modifiedObject)
	{
		if (modifiedObject == null)
		{
			return null;
		}
		IEntityMetaData metaData = this.entityMetaDataProvider.getMetaData(modifiedObject.getClass());
		Object id = metaData.getIdMember().getValue(modifiedObject, false);
		return this.getUnmodifiedObject(metaData.getEntityType(), id);
	}

}
