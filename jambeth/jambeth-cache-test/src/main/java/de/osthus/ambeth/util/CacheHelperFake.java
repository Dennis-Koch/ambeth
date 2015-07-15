package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public class CacheHelperFake implements ICacheHelper, ICachePathHelper
{
	public Class<?> entityType;
	public String memberToInitialize;
	public ISet<AppendableCachePath> cachePaths;

	@Autowired
	protected IServiceContext beanContext;

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize, ISet<AppendableCachePath> cachePaths)
	{
		this.entityType = entityType;
		this.memberToInitialize = memberToInitialize;
		this.cachePaths = cachePaths;
	}

	@Override
	public Object createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType)
	{
		return null;
	}

	@Override
	public Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType, Class<?> elementType)
	{
		return null;
	}

	@Override
	public Object[] extractPrimitives(IEntityMetaData metaData, Object obj)
	{
		return null;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj)
	{
		return null;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues)
	{
		return null;
	}

	@Override
	public PrefetchPath copyAppendableToCachePath(AppendableCachePath cachePath)
	{
		return null;
	}

	@Override
	public PrefetchPath[] copyAppendableToCachePath(ISet<AppendableCachePath> children)
	{
		return null;
	}

	@Override
	public AppendableCachePath copyCachePathToAppendable(PrefetchPath cachePath)
	{
		return null;
	}

	@Override
	public IPrefetchState ensureInitializedRelations(Object objects, ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps)
	{
		return null;
	}

	@Override
	public void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other)
	{
	}
}
