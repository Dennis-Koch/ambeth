package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public class CacheHelperFake implements ICacheHelper, ICachePathHelper
{
	public Class<?> entityType;
	public String memberToInitialize;
	public List<CachePath> cachePaths;

	@Autowired
	protected IServiceContext beanContext;

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize, List<CachePath> cachePaths)
	{
		this.entityType = entityType;
		this.memberToInitialize = memberToInitialize;
		this.cachePaths = cachePaths;
	}

	@Override
	public <V extends List<CachePath>> IPrefetchState ensureInitializedRelations(Object objects, IMap<Class<?>, V> typeToMembersToInitialize)
	{
		return null;
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
	public IPrefetchConfig createPrefetch()
	{
		return beanContext.registerAnonymousBean(PrefetchConfig.class).finish();
	}

	@Override
	public IPrefetchState prefetch(Object objects, IMap<Class<?>, List<String>> typeToMembersToInitialize)
	{
		return null;
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return null;
	}
}
