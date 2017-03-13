package com.koch.ambeth.cache.mock;

import java.util.List;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.model.IMethodDescription;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class MergeServiceMock implements IMergeService
{
	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		return null;
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		return null;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return null;
	}
}
