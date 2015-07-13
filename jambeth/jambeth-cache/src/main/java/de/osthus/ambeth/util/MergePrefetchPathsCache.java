package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.Tuple3KeyHashMap;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class MergePrefetchPathsCache
{
	public static final PrefetchPath[] EMPTY_PREFETCH_PATHS = new PrefetchPath[0];

	private final IEntityMetaDataProvider entityMetaDataProvider;

	private final Tuple3KeyHashMap<Class<?>, PrefetchPath[], IMap<Class<?>, PrefetchPath[]>, PrefetchPath[]> prefetchPathsMap = new Tuple3KeyHashMap<Class<?>, PrefetchPath[], IMap<Class<?>, PrefetchPath[]>, PrefetchPath[]>();

	public MergePrefetchPathsCache(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public PrefetchPath[] mergePrefetchPaths(Class<?> entityType, PrefetchPath[] relativePrefetchPath, IMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPaths)
	{
		if (entityTypeToPrefetchPaths == null)
		{
			return EMPTY_PREFETCH_PATHS;
		}
		if (relativePrefetchPath == null)
		{
			relativePrefetchPath = EMPTY_PREFETCH_PATHS;
		}
		PrefetchPath[] prefetchPaths = prefetchPathsMap.get(entityType, relativePrefetchPath, entityTypeToPrefetchPaths);
		if (prefetchPaths != null)
		{
			return prefetchPaths;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null)
		{
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, relativePrefetchPath);
			return relativePrefetchPath;
		}
		PrefetchPath[] absolutePrefetchPath = entityTypeToPrefetchPaths.get(metaData.getEntityType());
		if (absolutePrefetchPath == null)
		{
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, relativePrefetchPath);
			return relativePrefetchPath;
		}
		if (relativePrefetchPath.length == 0)
		{
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, absolutePrefetchPath);
			return absolutePrefetchPath;
		}
		HashMap<String, PrefetchPath> tempPrefetchPaths = HashMap.create(relativePrefetchPath.length + absolutePrefetchPath.length);
		for (PrefetchPath prefetchPath : relativePrefetchPath)
		{
			tempPrefetchPaths.putIfNotExists(prefetchPath.memberName, prefetchPath);
		}
		for (PrefetchPath prefetchPath : absolutePrefetchPath)
		{
			tempPrefetchPaths.putIfNotExists(prefetchPath.memberName, prefetchPath);
		}
		prefetchPaths = tempPrefetchPaths.toArray(PrefetchPath.class);
		prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, prefetchPaths);
		return prefetchPaths;
	}
}
