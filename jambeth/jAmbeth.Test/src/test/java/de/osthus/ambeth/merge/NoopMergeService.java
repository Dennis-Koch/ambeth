package de.osthus.ambeth.merge;

import java.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.service.IMergeService;

public class NoopMergeService implements IMergeService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		throw new UnsupportedOperationException("Should not be called");
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		throw new UnsupportedOperationException("Should not be called");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		throw new UnsupportedOperationException("Should not be called");
	}
}
