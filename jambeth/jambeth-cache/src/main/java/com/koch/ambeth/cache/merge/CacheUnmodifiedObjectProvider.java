package com.koch.ambeth.cache.merge;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
