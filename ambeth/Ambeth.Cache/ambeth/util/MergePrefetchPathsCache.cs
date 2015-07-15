using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using System;
namespace De.Osthus.Ambeth.Util
{
	public class MergePrefetchPathsCache
	{
		public static readonly PrefetchPath[] EMPTY_PREFETCH_PATHS = new PrefetchPath[0];

		private readonly IEntityMetaDataProvider entityMetaDataProvider;

		private readonly Tuple3KeyHashMap<Type, PrefetchPath[], IMap<Type, PrefetchPath[]>, PrefetchPath[]> prefetchPathsMap = new Tuple3KeyHashMap<Type, PrefetchPath[], IMap<Type, PrefetchPath[]>, PrefetchPath[]>();

		public MergePrefetchPathsCache(IEntityMetaDataProvider entityMetaDataProvider)
		{
			this.entityMetaDataProvider = entityMetaDataProvider;
		}

		public PrefetchPath[] MergePrefetchPaths(Type entityType, PrefetchPath[] relativePrefetchPath, IMap<Type, PrefetchPath[]> entityTypeToPrefetchPaths)
		{
			if (entityTypeToPrefetchPaths == null)
			{
				return EMPTY_PREFETCH_PATHS;
			}
			if (relativePrefetchPath == null)
			{
				relativePrefetchPath = EMPTY_PREFETCH_PATHS;
			}
			PrefetchPath[] prefetchPaths = prefetchPathsMap.Get(entityType, relativePrefetchPath, entityTypeToPrefetchPaths);
			if (prefetchPaths != null)
			{
				return prefetchPaths;
			}
			IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(entityType, true);
			if (metaData == null)
			{
				prefetchPathsMap.Put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, relativePrefetchPath);
				return relativePrefetchPath;
			}
			PrefetchPath[] absolutePrefetchPath = entityTypeToPrefetchPaths.Get(metaData.EntityType);
			if (absolutePrefetchPath == null)
			{
				prefetchPathsMap.Put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, relativePrefetchPath);
				return relativePrefetchPath;
			}
			if (relativePrefetchPath.Length == 0)
			{
				prefetchPathsMap.Put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, absolutePrefetchPath);
				return absolutePrefetchPath;
			}
			HashMap<String, PrefetchPath> tempPrefetchPaths = HashMap<String, PrefetchPath>.Create(relativePrefetchPath.Length + absolutePrefetchPath.Length);
			foreach (PrefetchPath prefetchPath in relativePrefetchPath)
			{
				tempPrefetchPaths.PutIfNotExists(prefetchPath.memberName, prefetchPath);
			}
			foreach (PrefetchPath prefetchPath in absolutePrefetchPath)
			{
				tempPrefetchPaths.PutIfNotExists(prefetchPath.memberName, prefetchPath);
			}
			prefetchPaths = tempPrefetchPaths.ToArray();
			prefetchPathsMap.Put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths, prefetchPaths);
			return prefetchPaths;
		}
	}
}