package com.koch.ambeth.cache.merge;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.IUnmodifiedObjectProvider;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.ParamChecker;

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
